package seb40_main_012.back.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seb40_main_012.back.book.BookDto;
import seb40_main_012.back.book.entity.Book;
import seb40_main_012.back.common.comment.entity.CommentType;
import seb40_main_012.back.config.auth.dto.LoginDto;
import seb40_main_012.back.bookCollection.dto.BookCollectionDto;
import seb40_main_012.back.bookCollection.entity.BookCollection;
import seb40_main_012.back.bookCollection.repository.BookCollectionRepository;
import seb40_main_012.back.common.comment.CommentDto;
import seb40_main_012.back.common.comment.CommentRepository;
import seb40_main_012.back.common.comment.entity.Comment;
import seb40_main_012.back.dto.ListResponseDto;
import seb40_main_012.back.dto.SingleResponseDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper mapper;
    private final CommentRepository commentRepository;
    private final PairingRepository pairingRepository;
    private final BookCollectionRepository collectionRepository;


    @PostMapping("/users")
    public ResponseEntity postUser(@Valid @RequestBody UserDto.PostDto postdto) {
        User user = mapper.userPostToUser(postdto);

        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.userToUserResponse(createdUser)), HttpStatus.CREATED);
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
    public UserInfoDto.Response patchUserInfo(@RequestBody UserInfoDto.Post request) {
        User editedUser = userService.editUserInfo(request.toEntity(), request.getCategory());
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
    @ResponseStatus(HttpStatus.OK)
    public CommentDto.CommentList getUserComment() {
        List<Comment> comments = userService.getUserComment();
        List<CommentDto.BookComment> bookComments = new ArrayList<>();
        List<CommentDto.PairingComment> pairingComments = new ArrayList<>();
        List<CommentDto.CollectionComment> collectionComments = new ArrayList<>();

        comments.forEach(
                x -> {
                    if (x.getCommentType() == CommentType.BOOK) {
                        bookComments.add(CommentDto.BookComment.of(x));
                    } else if (x.getCommentType() == CommentType.PAIRING) {
                        pairingComments.add(CommentDto.PairingComment.of(x));
                    } else if (x.getCommentType() == CommentType.BOOK_COLLECTION) {
                        collectionComments.add(CommentDto.CollectionComment.of(x));
                    }
                }
        );
        Long listCount = commentRepository.countBy();
        return CommentDto.CommentList.of(bookComments, pairingComments, collectionComments);
    }

    @GetMapping("/mypage/userPairing")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<PairingDto.UserPairing> getUserPairing() {
        List<Pairing> pairings = userService.getUserPairing();
        List<PairingDto.UserPairing> pairingDto = pairings.stream().map(x -> PairingDto.UserPairing.of(x)).collect(Collectors.toList());
        Long listCount = pairingRepository.countBy();
        return new ListResponseDto<>(listCount, pairingDto);
    }

    @GetMapping("/mypage/userCollection")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<BookCollectionDto.UserCollection> getUserBookCollection() {
        List<BookCollection> collections = userService.getUserCollection();
        List<BookCollectionDto.UserCollection> collectionDto = collections.stream().map(x -> BookCollectionDto.UserCollection.of(x)).collect(Collectors.toList());
        User findUser = userService.getLoginUser();
        Long listCount = collectionRepository.countByUserUserId(findUser.getUserId());
        return new ListResponseDto<>(listCount, collectionDto);
    }


    @GetMapping("/mypage/bookmark/collection")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<BookCollectionDto.BookmarkedCollection> getBookmarkByBookCollection() {
        List<BookCollection> collections = userService.getBookmarkByBookCollection();
        List<BookCollectionDto.BookmarkedCollection> bookmarkedCollectionDto = collections.stream().map(x -> BookCollectionDto.BookmarkedCollection.of(x)).collect(Collectors.toList());
        User findUser = userService.getLoginUser();
        Long listCount = collectionRepository.countByUserUserId(findUser.getUserId());
        return new ListResponseDto<>(listCount, bookmarkedCollectionDto);
    }

    @GetMapping("/mypage/bookmark/pairing")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<PairingDto.BookmarkedPairing> getBookMarkByPairing() {
        List<Pairing> pairings = userService.getBookmarkByPairing();
        List<PairingDto.BookmarkedPairing> pairingDto = pairings.stream().map(x -> PairingDto.BookmarkedPairing.of(x)).collect(Collectors.toList());
        Long listCount = pairingRepository.countBy();
        return new ListResponseDto<>(listCount, pairingDto);
    }

    @GetMapping("/mypage/bookmark/book")
    @ResponseStatus(HttpStatus.OK)
    public ListResponseDto<BookDto.BookmarkedBook> getBookMarkByBook() {
        List<Book> books = userService.getBookmarkByBook();
        List<BookDto.BookmarkedBook> bookDto = books.stream().map(x -> BookDto.BookmarkedBook.of(x)).collect(Collectors.toList());
        Long listCount = pairingRepository.countBy();
        return new ListResponseDto<>(listCount, bookDto);
    }


    @PatchMapping("/users/firstLogin")
    public ResponseEntity patchUserOnFirstLogin(@Valid @RequestBody LoginDto.PatchDto patchDto) {
        User user = userService.updateOnFirstLogin(patchDto);
        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.userToFirstLoginResponse(user)), HttpStatus.OK);
    }

}
