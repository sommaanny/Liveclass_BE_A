package com.liveclass.be_a.domain.enrollment.service;

import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.course.repository.CourseRepository;
import com.liveclass.be_a.domain.enrollment.repository.EnrollmentRepository;
import com.liveclass.be_a.domain.member.entity.Member;
import com.liveclass.be_a.domain.member.repository.MemberRepository;
import com.liveclass.be_a.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EnrollmentConcurrencyTest {

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Test
    @Timeout(10)
    @DisplayName("동시 수강 신청 시 강의 정원을 초과해서 저장되지 않는다")
    void enrollment_concurrency_capacity_limit() throws Exception {
        int capacity = 5;
        int threadCount = 20;
        Long courseId = createOpenCourse(capacity);
        List<Long> memberIds = createMembers(threadCount);

        ConcurrencyResult result = runConcurrently(threadCount, index ->
                enrollmentService.enrollment(courseId, memberIds.get(index))
        );

        int savedEnrollmentCount = countEnrollments(courseId);

        assertThat(result.unexpectedFailures()).isEmpty();
        assertThat(result.successCount()).isEqualTo(capacity);
        assertThat(result.businessFailures()).hasSize(threadCount - capacity);
        assertThat(savedEnrollmentCount).isEqualTo(capacity);
    }

    @Test
    @Timeout(10)
    @DisplayName("같은 회원이 동시에 같은 강의를 신청해도 하나만 저장된다")
    void enrollment_concurrency_duplicate_member() throws Exception {
        int threadCount = 10;
        Long courseId = createOpenCourse(threadCount);
        Long memberId = createMembers(1).get(0);

        ConcurrencyResult result = runConcurrently(threadCount, index ->
                enrollmentService.enrollment(courseId, memberId)
        );

        int savedEnrollmentCount = countEnrollments(courseId);

        assertThat(result.unexpectedFailures()).isEmpty();
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.businessFailures()).hasSize(threadCount - 1);
        assertThat(savedEnrollmentCount).isEqualTo(1);
    }

    private Long createOpenCourse(int capacity) {
        return transactionTemplate.execute(status -> {
            Course course = Course.builder()
                    .creatorId(100L)
                    .title("동시성 테스트 강의")
                    .description("동시성 테스트용 강의입니다.")
                    .price(10_000)
                    .capacity(capacity)
                    .startDate(LocalDateTime.now().plusDays(1))
                    .endDate(LocalDateTime.now().plusDays(30))
                    .build();

            courseRepository.save(course);
            course.openCourse();

            return course.getId();
        });
    }

    private List<Long> createMembers(int count) {
        return transactionTemplate.execute(status -> {
            List<Long> memberIds = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                Member member = new Member("동시성 테스트 회원 " + i);
                memberRepository.save(member);
                memberIds.add(member.getId());
            }

            return memberIds;
        });
    }

    private int countEnrollments(Long courseId) {
        return transactionTemplate.execute(status -> enrollmentRepository.countEnrollments(courseId));
    }

    private ConcurrencyResult runConcurrently(int threadCount, ConcurrentTask task) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        Queue<BusinessException> businessFailures = new ConcurrentLinkedQueue<>();
        Queue<Throwable> unexpectedFailures = new ConcurrentLinkedQueue<>();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int index = i;

            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();

                try {
                    task.run(index);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    businessFailures.add(e);
                } catch (Throwable e) {
                    unexpectedFailures.add(e);
                }

                return null;
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();

        return new ConcurrencyResult(successCount.get(), businessFailures, unexpectedFailures);
    }

    @FunctionalInterface
    private interface ConcurrentTask {
        void run(int index) throws Exception;
    }

    private record ConcurrencyResult(
            int successCount,
            Queue<BusinessException> businessFailures,
            Queue<Throwable> unexpectedFailures
    ) {
    }
}
