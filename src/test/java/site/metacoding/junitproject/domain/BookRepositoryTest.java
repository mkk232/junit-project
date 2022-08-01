package site.metacoding.junitproject.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.booleanThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import site.metacoding.junitproject.service.BookService;

// Controller -> Service -> Repository
//  (3)             (2)         (1) DB쪽 관련 테스트

// 책 등록 될때, author가 null이다.

@DataJpaTest // DB와 관련된 컴포넌트만 메모리에 로딩
public class BookRepositoryTest {
    
    @Autowired // DI
    private BookRepository bookRepository;

    //@BeforeAll // 테스트 시작 전에 한번만 실행
    @BeforeEach // 각 테스트 시작 전에 한번씩 실행
    public void 데이터준비() {
        String title = "junit";
        String author = "겟인데어";
        Book book = Book.builder()
                        .title(title)
                        .author(author)
                        .build();
        bookRepository.save(book);
    }
    // 가정 1 : [데이터준비() + 1 책등록] (T), [데이터준비 + 2 책목록보기] (T) ==> 사이즈 1 (검증 완료)
    // 가정 2 : [데이터준비() + 1 책등록 + 데이터준비() + 2 책목록보기] (T) ==> 사이즈 2 (검증 실패)

    // 1. 책 등록
    @Test
    public void 책등록_test() {
        // given (데이터 준비)
        String title = "junit5";
        String author = "메타코딩";
        Book book = Book.builder()
                        .title(title)
                        .author(author)
                        .build();

        // when (테스트 실행)
        Book bookPS = bookRepository.save(book);

        // then (검증)
        assertEquals("junit5", bookPS.getTitle());
        assertEquals("메타코딩", bookPS.getAuthor());

    } // 트랜잭션 종료 (저장된 데이터를 초기화함)

    // 2. 책 목록보기
    @Test
    public void 책목록보기_test() {

        // given
        String title = "junit";
        String author = "겟인데어";

        // when
        List<Book> bookPS = bookRepository.findAll();

        System.out.println("사이즈 : ====================     : " + bookPS.size());

        // then
        assertEquals(title, bookPS.get(0).getTitle());
        assertEquals(author, bookPS.get(0).getAuthor());

    }// 트랜잭션 종료 (저장된 데이터를 초기화함)

    // 3. 책 한건보기
    // 해당 메서드가 실행되기 전 마다 sql파일이 실행이 됨.
    @Sql("classpath:db/tableInit.sql")
    @Test
    public void 책한건보기_test() {
        // given
        String title = "junit";
        String author = "겟인데어";

        // when
        Book bookPS = bookRepository.findById(1L).get();

        // then
        assertEquals(title, bookPS.getTitle());
        assertEquals(author, bookPS.getAuthor());
    }
    // 4. 책 삭제
    @Sql("classpath:db/tableInit.sql")
    @Test
    public void 책삭제_test() {
        // given
        Long id = 1L;

        // when
        bookRepository.deleteById(id);

        // then
        // Optional<Book> bookPS = bookRepository.findById(id).isPresent();

        // isPresent() = null이 아니면, 존재한다면
        // assertFalse = false면 성공, true면 실패
        assertFalse(bookRepository.findById(id).isPresent());

    }

    // 1, junit, 겟인데어
    // 5. 책 수정
    @Sql("classpath:db/tableInit.sql")
    @Test
    public void 책수정_Test() {
        // given
        Long id = 1L;
        String title = "JUnit5";
        String author = "메타코딩";
        Book book = new Book(id, title, author);

        // when
        // bookRepository.findAll().stream()
        //                     .forEach(b -> 
        //                     {
        //                         System.out.println(b.getId());
        //                         System.out.println(b.getTitle());
        //                         System.out.println(b.getAuthor());
        //                         System.out.println("1. ==================");
        //                     });

        Book bookPS = bookRepository.save(book);
        // bookRepository.findAll().stream()
        //                     .forEach(b -> 
        //                     {
        //                         System.out.println(b.getId());
        //                         System.out.println(b.getTitle());
        //                         System.out.println(b.getAuthor());
        //                         System.out.println("2. ==================");
        //                     });

        // then
        assertEquals(id, bookPS.getId());
        assertEquals(title, bookPS.getTitle());
        assertEquals(author, bookPS.getAuthor());
    }
}
 
/*
 * JUnit 테스트
 * 
 * 1. 테스트 메서드 3개가 있다. (순서 보장이 안된다.) - Order()어노테이션을 사용해야함
 * (1) 메서드1
 * (2) 메서드2
 * (3) 메서드3
 * 가장 위에있다고 먼저 실행되는거 아님. 
 * 
 * 2. 테스트 메서드가 하나 실행 후 종료되면 데이터가 초기화된다. - Transactional() 어노테이션
 * (1) 1건
 * (2) 2건
 * -> 트랜잭션 종료 -> 데이터 초기화
 * *** primary key auto_increment 값이 초기화가 안됨. 
 */