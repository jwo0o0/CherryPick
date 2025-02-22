package seb40_main_012.back.bookCollection.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import seb40_main_012.back.advice.BusinessLogicException;
import seb40_main_012.back.advice.ExceptionCode;
import seb40_main_012.back.book.BookRepository;
import seb40_main_012.back.book.BookService;
import seb40_main_012.back.book.entity.Book;
import seb40_main_012.back.book.entity.Genre;
import seb40_main_012.back.book.BookSpecification;
import seb40_main_012.back.bookCollection.entity.BookCollection;
import seb40_main_012.back.bookCollection.entity.BookCollectionLike;
import seb40_main_012.back.bookCollection.entity.BookCollectionTag;
import seb40_main_012.back.bookCollection.entity.Tag;
import seb40_main_012.back.bookCollection.repository.BookCollectionLikeRepository;
import seb40_main_012.back.bookCollection.repository.BookCollectionRepository;
import seb40_main_012.back.bookCollection.repository.BookCollectionTagRepository;
import seb40_main_012.back.bookCollection.repository.TagRepository;
import seb40_main_012.back.bookCollectionBook.BookCollectionBook;
import seb40_main_012.back.bookCollectionBook.BookCollectionBookRepository;
import seb40_main_012.back.common.bookmark.BookmarkRepository;
import seb40_main_012.back.user.entity.User;
import seb40_main_012.back.user.entity.UserCategory;
import seb40_main_012.back.user.repository.CategoryRepository;
import seb40_main_012.back.user.repository.UserCategoryRepository;
import seb40_main_012.back.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookCollectionService {
    private final UserService userService;
    private final BookService bookService;
    private final BookCollectionRepository collectionRepository;
    private final BookCollectionTagRepository collectionTagRepository;
    private final BookCollectionLikeRepository collectionLikeRepository;
    private final BookmarkRepository collectionBookmarkRepository;
    private final BookCollectionBookRepository collectionBookRepository;
    private final BookRepository bookRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public BookCollection postCollection(BookCollection collection, List<String> tags) {

        User findUser = userService.getLoginUser();

        Long userId = findUser.getUserId();

        collection.setCollectionTag();


        tags.forEach(
                x -> {
                    Tag newTag = new Tag(x);
                    tagRepository.save(newTag);
                    BookCollectionTag collectionTag = new BookCollectionTag(collection, newTag);
                    collectionRepository.save(collection);
                    collectionTagRepository.save(collectionTag);
                    collection.addCollectionTag(collectionTag);
                    findUser.addBookCollection(collection);
                    collection.addUser(findUser);
                }
        );
        List<String> isbn = collection.getBookIsbn13();
        isbn.forEach(
                x -> {
                    Book newBook = bookService.updateView(x);
                    BookCollectionBook findCollectionBook = new BookCollectionBook(newBook,collection);
                    collectionBookRepository.save(findCollectionBook);
                    collection.addCollectionBook(findCollectionBook);
//                    if(bookRepository.findByIsbn13(x)!=null){
//                        Book findBook = bookRepository.findByIsbn13(x).orElseThrow(() -> new BusinessLogicException(ExceptionCode.BOOK_NOT_FOUND));
//                        BookCollectionBook findCollectionBook = new BookCollectionBook(findBook,collection);
//                        collectionBookRepository.save(findCollectionBook);
//                        collection.addCollectionBook(findCollectionBook);
//                    }else {
//                        Book newBook = bookService.updateView(x);
//                        BookCollectionBook findCollectionBook = new BookCollectionBook(newBook,collection);
//                        collectionBookRepository.save(findCollectionBook);
//                        collection.addCollectionBook(findCollectionBook);
//                    }

                }
        );

        return collection;
    }

    public BookCollection patchCollection(Long collectionId, BookCollection collection, List<String> tags) {


        User findUser = userService.getLoginUser();

        Long userId = findUser.getUserId();

        BookCollection bookCollection = findVerifiedCollection(collectionId);
//        collection.setCollectionTag();

        tags.forEach(
                x -> {
                    Tag newTag = new Tag(x);
                    tagRepository.save(newTag);
                    BookCollectionTag collectionTag = new BookCollectionTag(bookCollection, newTag);
                    collectionRepository.save(bookCollection);
                    collectionTagRepository.save(collectionTag);
                    bookCollection.addCollectionTag(collectionTag);
                    bookCollection.editCollection(collection);
                    findUser.addBookCollection(bookCollection);
                    bookCollection.addUser(findUser);
                }
        );
        return bookCollection;
    }


    //상세 조회 -> ISBN13 으로 db에서 책 별점 조회,없으면 알라딘 api에서 책 정보만 조회
    public BookCollection getCollection(Long collectionId) {
        BookCollection findBookCollection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.COLLECTION_NOT_FOUND));

        isUserLike(collectionId);
        isUserBookmark(collectionId);
        isUserCollection(collectionId);
        findBookCollection.setView(findBookCollection.getView() + 1);

        return findBookCollection;
    }

    public boolean likeCollection(Long collectionId) {
        User findUser = userService.getLoginUser();
        Long userId = findUser.getUserId();

        BookCollection findCollection = findVerifiedCollection(collectionId);
        Long count = collectionLikeRepository.count();
        BookCollectionLike collectionLike = collectionLikeRepository.findByUserUserIdAndBookCollectionCollectionId(userId, collectionId);
        try {
            if (collectionLike != null) {
                collectionLikeRepository.delete(collectionLike);
                count -= 1L;
                findCollection.setLikeCount(count);
            } else {
                BookCollectionLike bookCollectionLike = new BookCollectionLike(findUser, findCollection);
                collectionLikeRepository.save(bookCollectionLike);
                findUser.addCollectionLike(bookCollectionLike);
                count += 1L;
                findCollection.setLikeCount(count);
            }

            return true;
        } catch (BusinessLogicException e) {
            throw new BusinessLogicException(ExceptionCode.FAIL_TO_LIKE);
        }
    }

    public void deleteCollection(Long collectionId) {
        User findUser = userService.getLoginUser();

        Long userId = findUser.getUserId();

        collectionRepository.deleteById(collectionId);
    }

    public void isUserLike(Long collectionId){
        User findUser = userService.getLoginUser();
        BookCollection bookCollection = findVerifiedCollection(collectionId);
        if(collectionLikeRepository.findByUserUserIdAndBookCollectionCollectionId(findUser.getUserId(),collectionId)==null)
            bookCollection.setUserLike(false);
        else bookCollection.setUserLike(true);
    }

    public void isUserBookmark(Long collectionId){
        User findUser = userService.getLoginUser();
        BookCollection bookCollection = findVerifiedCollection(collectionId);
        if(collectionBookmarkRepository.findByUserAndBookCollection(findUser,bookCollection)==null)
            bookCollection.setUserBookmark(false);
        else bookCollection.setUserBookmark(true);
    }
    public void isUserCollection(Long collectionId){
        User findUser = userService.getLoginUser();
        BookCollection bookCollection = findVerifiedCollection(collectionId);
        User collectionUser = bookCollection.getUser();

        if(findUser.getUserId()==collectionUser.getUserId())
            bookCollection.setUserCollection(true);
        else bookCollection.setUserCollection(false);
    }


    public BookCollection findVerifiedCollection(Long collectionId) {
        BookCollection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.COLLECTION_NOT_FOUND));
        return collection;
    }

    public List<BookCollection> findCollectionByUserCategory(){
        User findUser = userService.getLoginUser();

        Long userId = findUser.getUserId();
        String userCategory = findUser.getCategories().get(0).getCategory().getGenre().toString();
        System.out.println("HERE>" + userCategory);

//        Category category = categoryRepository.findByGenre(Genre.ART);

        Tag tag = tagRepository.findByTagName(userCategory).orElseThrow(() -> new BusinessLogicException(ExceptionCode.NOT_FOUND));
        System.out.println("HERE>" + tag.getTagName());

        List<BookCollectionTag> collectionTag = collectionTagRepository.findByTag(tag);
        List<BookCollection> collections = collectionTag.stream().map(BookCollectionTag::getBookCollection).collect(Collectors.toList());
        return collections;
    }

    public List<BookCollection> findCollectionByCollectionTag() {
        User findUser = userService.getLoginUser();

        Long userId = findUser.getUserId();
        String tagName = "겨울";
        Tag tag = tagRepository.findByTagName(tagName).orElseThrow(() -> new BusinessLogicException(ExceptionCode.NOT_FOUND));
        List<BookCollectionTag> collectionTag = collectionTagRepository.findByTag(tag);
        List<BookCollection> collections = collectionTag.stream().map(BookCollectionTag::getBookCollection).collect(Collectors.toList());
        return collections;
    }


    public BookCollection findCollectionByAuthor(){
        String author = "양귀자 (지은이)";

        String title = "양귀자 모음";
        String content = "";

        //저자 이름으로 조회한 책 isbn으로 updateView() 통해 책 db 저장
        List<Book> books = bookRepository.findWritersBooks(author);

        List<BookCollectionBook> collectionBooks = new ArrayList<>();
        books.forEach(
                x -> {
                    BookCollectionBook collectionBook = BookCollectionBook.builder().book(x).build();
                    collectionBookRepository.save(collectionBook);
                    collectionBooks.add(collectionBook);
                }
        );
        return BookCollection.builder()
                .title(title)
                .content(content)
                .collectionBooks(collectionBooks)
                .build();
    }

    public BookCollection findCollectionByCritic(){
        return findVerifiedCollection(53L);

    }


    public List<Book> findBooks(List<String> isbn) {
        List<Book> findBooks = new ArrayList<>();
        isbn.forEach(
                x -> {
                    findBooks.add(bookService.findBook(x));
                    //save()
                }
        );
        return findBooks;
    }

    public List<BookCollection> findCollectionByUserCategory2(){
        User loginUser = userService.getLoginUser();
        List<UserCategory> userCategory = userCategoryRepository.findAllByUser(loginUser);
        List<Genre> genres = userCategory.stream()
                .map(userCate -> userCate.getCategory().getGenre())
                .collect(Collectors.toList());

        Specification<Book> bookSpec = null;
        for(Genre genre : genres) {
            Specification<Book> bookSpecWithGenre = BookSpecification.findBookByGenre(genre);
            bookSpec = (bookSpec == null) ? bookSpecWithGenre : bookSpec.or(bookSpecWithGenre);
        }
        bookSpec.and(BookSpecification.isPresentCollection());
        List<Book> books = bookRepository.findAll(bookSpec);

        List<BookCollection> bookCollections = new ArrayList<>();
        for(Book book : books) {
            bookCollections.addAll(collectionBookRepository.findAllByBook(book).stream()
                    .map(bookCollectionBook -> bookCollectionBook.getBookCollection())
                    .filter(bcb -> !bookCollections.contains(bcb))
                    .collect(Collectors.toList()));
        }

        return bookCollections;
    }

}
