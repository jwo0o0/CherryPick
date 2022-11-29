package seb40_main_012.back.user.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import seb40_main_012.back.book.BookDto;
import seb40_main_012.back.book.BookRepository;
import seb40_main_012.back.book.entity.Book;
import seb40_main_012.back.common.bookmark.BookmarkRepository;
import seb40_main_012.back.common.bookmark.BookmarkType;
import seb40_main_012.back.common.comment.CommentMapper;
import seb40_main_012.back.common.comment.CommentService;
import seb40_main_012.back.common.comment.entity.CommentType;
import seb40_main_012.back.common.image.AwsS3Service;
import seb40_main_012.back.config.auth.dto.LoginDto;
import seb40_main_012.back.bookCollection.dto.BookCollectionDto;
import seb40_main_012.back.bookCollection.entity.BookCollection;
import seb40_main_012.back.bookCollection.repository.BookCollectionRepository;
import seb40_main_012.back.common.comment.CommentDto;
import seb40_main_012.back.common.comment.CommentRepository;
import seb40_main_012.back.common.comment.entity.Comment;
import seb40_main_012.back.dto.ListResponseDto;
import seb40_main_012.back.dto.SingleResponseDto;
import seb40_main_012.back.email.EmailSenderService;
import seb40_main_012.back.pairing.PairingDto;
import seb40_main_012.back.pairing.PairingRepository;
import seb40_main_012.back.pairing.entity.Pairing;
import seb40_main_012.back.user.dto.UserDto;
import seb40_main_012.back.user.dto.UserInfoDto;
import seb40_main_012.back.user.entity.User;
import seb40_main_012.back.user.mapper.UserMapper;
import seb40_main_012.back.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper mapper;
    private final CommentMapper commentMapper;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final PairingRepository pairingRepository;
    private final BookCollectionRepository collectionRepository;
    private final BookRepository bookRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AwsS3Service awsS3Service;

    @PostMapping("/users")
    public ResponseEntity postUser(@Valid @RequestBody UserDto.PostDto postDto) {

        User user = mapper.userPostToUser(postDto);
        User createdUser = userService.createUser(user);
        UserDto.ResponseDto response = mapper.userToUserResponse(createdUser);

        return new ResponseEntity<>(
                new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    @PostMapping("/mypage/verify/nickName")
    public boolean verifyNickName(@Valid @RequestBody UserDto.Profile request) {
        return userService.verifyNickName(request.getNickName());
    }

    @PostMapping("/mypage/verify/email")
    public boolean verifyEmail(@Valid @RequestBody UserDto.EmailDto emailDto) {
        return userService.verifyEmail(emailDto.getEmail());
    }

//    @PatchMapping("/mypage/nickname")
//    @ResponseStatus(HttpStatus.OK)
//    public void patchNickName(@RequestBody UserDto.Profile request) {
//        userService.updateNickName(request.getNickName());
//    }

    @PostMapping("/mypage/password/current")
    @ResponseStatus(HttpStatus.OK)
    public boolean verifyPassword(@RequestBody UserDto.Password currentPassword) {
        return userService.verifyPassword(currentPassword.getPassword());
    }

    @PatchMapping("/mypage/password/update")
    @ResponseStatus(HttpStatus.OK)
    public void patchPassword(@RequestBody UserDto.Password request) {
        userService.updatePassword(request.getPassword());
    }

    @PatchMapping("/mypage/userInfo")
    @ResponseStatus(HttpStatus.OK)
    public UserInfoDto.Response patchUserInfo(
            @RequestParam(value = "image") @Nullable MultipartFile file,
            @RequestPart UserInfoDto.Post request) throws Exception {

        User editedUser = userService.editUserInfo(request.toEntity(), request.getCategory());

        if (editedUser.getS3ProfileImage() == null && file == null) {

            editedUser.setProfileImage(null);

        } else if (editedUser.getS3ProfileImage() == null && file != null) {

            String imagePath = awsS3Service.uploadProfileImageToS3(file);
            editedUser.setProfileImage(imagePath);

        } else if (editedUser.getS3ProfileImage() != null && file == null) {

            editedUser.setProfileImage(null);

        } else if (editedUser.getS3ProfileImage() != null && file != null) {

//            awsS3Service.removeFromS3(pairingService.findPairing(pairingId).getImagePath()); // 기존 이미지 삭제
            String imagePath = awsS3Service.uploadProfileImageToS3(file); // 새 이미지 저장
            editedUser.setProfileImage(imagePath);
        }

        userService.updateNickName(request.getNickname());
        return UserInfoDto.Response.of(editedUser);
    }

    @GetMapping("/users/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity getUser(@PathVariable("user_id") @Positive Long userId) {

        User user = userService.findUser();

        UserDto.ResponseDto response = mapper.userToUserResponse(user);

        return new ResponseEntity<>(
                new SingleResponseDto<>(response), HttpStatus.OK
        );
    }

    //    @PatchMapping
//    public void patchImage(){}
//
//    @PatchMapping //프사 수정
//    public UserDto.ResponseDto patchProfileImage(){}
//
    @DeleteMapping("/mypage")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean deleteUser() {
        return userService.deleteUser();
    }


    /**
     * 조회 API
     */
//    @GetMapping("/mypage/nickName")
//    @ResponseStatus(HttpStatus.OK)
//    public UserDto.ProfileResponse getNickName(){
//        User findUser = userService.getLoginUser();
//        User user = userService.findVerifiedUser(findUser.getUserId());
//        return new UserDto.ProfileResponse(user.getNickName());
//    }
    @GetMapping("/mypage/userInfo")
    @ResponseStatus(HttpStatus.OK)
    public UserInfoDto.Response getUserInfo() {
        User findUser = userService.getLoginUser();
        User user = userService.findVerifiedUser(findUser.getUserId());
        return UserInfoDto.Response.of(user);
    }

    @GetMapping("/mypage/userComment")
    public ResponseEntity getUserComment() {
        User findUser = userService.getLoginUser();
        List<Comment> findComments = commentService.findMyCommentAll();
        List<CommentDto.myPageResponse> responses = commentMapper.commentsToMyPageResponse(findComments);
        Long listCount = commentRepository.countByUser(findUser);
        return new ResponseEntity<>(
                new ListResponseDto<>(listCount,responses), HttpStatus.OK);
    }

//    @GetMapping("/mypage/userComment")
//    @ResponseStatus(HttpStatus.OK)
//    public CommentDto.CommentList getUserComment() {
//        List<Comment> comments = userService.getUserComment();
//        List<CommentDto.BookComment> bookComments = new ArrayList<>();
//        List<CommentDto.PairingComment> pairingComments = new ArrayList<>();
//        List<CommentDto.CollectionComment> collectionComments = new ArrayList<>();
//
//        comments.forEach(
//                x -> {
//                    if (x.getCommentType() == CommentType.BOOK) {
//                        bookComments.add(CommentDto.BookComment.of(x));
//                    } else if (x.getCommentType() == CommentType.PAIRING) {
//                        pairingComments.add(CommentDto.PairingComment.of(x));
//                    } else if (x.getCommentType() == CommentType.BOOK_COLLECTION) {
//                        collectionComments.add(CommentDto.CollectionComment.of(x));
//                    }
//                }
//        );
//        Long listCount = commentRepository.countBy();
//        return CommentDto.CommentList.of(bookComments, pairingComments, collectionComments);
//    }

    @GetMapping("/mypage/userPairing")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<PairingDto.UserPairing> getUserPairing() {
        User findUser = userService.getLoginUser();
        List<Pairing> pairings = userService.getUserPairing();
        List<PairingDto.UserPairing> pairingDto = pairings.stream().map(x -> PairingDto.UserPairing.of(x)).collect(Collectors.toList());
        Long listCount = pairingRepository.countByUser(findUser);
        return new ListResponseDto<>(listCount, pairingDto);
    }


    @GetMapping("/mypage/userCollection")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<BookCollectionDto.UserCollection> getUserBookCollection() {
        List<BookCollection> collections = userService.getUserCollection();
        List<BookCollectionDto.UserCollection> collectionDto = collections.stream().map(x -> BookCollectionDto.UserCollection.of(x)).collect(Collectors.toList());
        User findUser = userService.getLoginUser();
        Long listCount = collectionRepository.countByUser(findUser);
        return new ListResponseDto<>(listCount, collectionDto);
    }

    @DeleteMapping("/mypage/userCollection/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllUserCollection(){
        userService.deleteAllUserCollection();
    }

    @GetMapping("/mypage/bookmark/collection")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<BookCollectionDto.BookmarkedCollection> getBookmarkByBookCollection() {
        List<BookCollection> collections = userService.getBookmarkByBookCollection();
        List<BookCollectionDto.BookmarkedCollection> bookmarkedCollectionDto = collections.stream().map(x -> BookCollectionDto.BookmarkedCollection.of(x)).collect(Collectors.toList());
        User findUser = userService.getLoginUser();
        Long listCount = bookmarkRepository.countByUserAndBookmarkType(findUser, BookmarkType.COLLECTION);
        return new ListResponseDto<>(listCount, bookmarkedCollectionDto);
    }

    @GetMapping("/mypage/bookmark/pairing")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<PairingDto.BookmarkedPairing> getBookMarkByPairing() {
        User findUser = userService.getLoginUser();
        List<Pairing> pairings = userService.getBookmarkByPairing();
        List<PairingDto.BookmarkedPairing> pairingDto = pairings.stream().map(x -> PairingDto.BookmarkedPairing.of(x)).collect(Collectors.toList());
        Long listCount = bookmarkRepository.countByUserAndBookmarkType(findUser, BookmarkType.PAIRING);
        return new ListResponseDto<>(listCount, pairingDto);
    }

    @GetMapping("/mypage/bookmark/book")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<BookDto.BookmarkedBook> getBookMarkByBook() {
        User findUser = userService.getLoginUser();
        List<Book> books = userService.getBookmarkByBook();
        List<BookDto.BookmarkedBook> bookDto = books.stream().map(x -> BookDto.BookmarkedBook.of(x)).collect(Collectors.toList());
        Long listCount = bookmarkRepository.countByUserAndBookmarkType(findUser, BookmarkType.BOOK);
        return new ListResponseDto<>(listCount, bookDto);
    }


    @PatchMapping("/users/firstLogin")
    public ResponseEntity patchUserOnFirstLogin(@Valid @RequestBody LoginDto.PatchDto patchDto) {
        User user = userService.updateOnFirstLogin(patchDto);
        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.userToFirstLoginResponse(user)), HttpStatus.OK);
    }

}
