package com.bookstore.repository;

import org.springframework.data.repository.CrudRepository;

import com.bookstore.domain.Book;

public interface BookRepository extends CrudRepository<Book, Long> {

}
