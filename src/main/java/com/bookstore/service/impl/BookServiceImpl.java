package com.bookstore.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookstore.domain.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.BookService;

@Service
public class BookServiceImpl implements BookService{
	
	@Autowired
	private BookRepository bookRepository;
	
	public List<Book> findAll() {
		return (List <Book>)bookRepository.findAll();
	}

	@Override
	public Book findById(Long id) {
		return bookRepository.findOne(id);
	}

	@Override
	public List<Book> findByCategory(String category) {
		return bookRepository.findByCategory(category);
	}

	@Override
	public List<Book> blurrySearch(String keyword) {
		return bookRepository.findByTitleContaining(keyword);
	}

}
