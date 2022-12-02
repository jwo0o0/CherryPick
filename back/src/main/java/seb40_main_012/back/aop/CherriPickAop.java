package seb40_main_012.back.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import seb40_main_012.back.config.auth.repository.RefreshTokenRepository;
import seb40_main_012.back.config.auth.service.UserDetailsServiceImpl;
import seb40_main_012.back.email.EmailSenderService;
import seb40_main_012.back.statistics.*;
import seb40_main_012.back.user.dto.UserDto;
import seb40_main_012.back.user.entity.User;
import seb40_main_012.back.user.entity.enums.AgeType;
import seb40_main_012.back.user.entity.enums.GenderType;
import seb40_main_012.back.user.repository.UserRepository;
import seb40_main_012.back.user.service.UserService;

import javax.servlet.FilterChain;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Aspect
@Component
@RequiredArgsConstructor
public class CherriPickAop {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailSenderService emailSenderService;
    private final StatisticsService statisticsService;
    private final StatisticsRepository statisticsRepository;
    private final StayTimeService stayTimeService;
    private final StayTimeRepository stayTimeRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @AfterReturning(value = "execution(* seb40_main_012.back.user.controller.UserController.postUser(..)) && args(postDto))", returning = "response")
    public void sendSignUpEmail(JoinPoint joinPoint, UserDto.PostDto postDto, ResponseEntity response) { // 회원가입 이메일

        try {
            emailSenderService.sendSignupEmail(postDto.getEmail());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @After(value = "execution(* seb40_main_012.back.book.BookController.carouselBooks())")
    public void createTable(JoinPoint joinPoint) { // 오늘의 첫 방문자가 있을 시 테이블 생성 및 정보 입력

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 유저 인증 정보

        if (statisticsRepository.findByDate(LocalDate.now()) == null && authentication.getName().equals("anonymousUser")) { // 오늘의 첫 방문자이면서 로그인 하지 않은 상태

            statisticsService.createTable(LocalDate.now());
            Statistics newStatistics = statisticsService.findByDate(LocalDate.now());
            newStatistics.setTotalVisitor(1);
            statisticsRepository.save(newStatistics);

        } else if (statisticsRepository.findByDate(LocalDate.now()) == null && !authentication.getName().equals("anonymousUser")) { // 오늘의 첫 방문자이면서 로그인 되어 있는 경우

            User findUser = userService.getLoginUser();
            List<String> genre = userService.getAllGenre(findUser);
            statisticsService.createTable(LocalDate.now());
            stayTimeService.createStayTimeTable(); // 체류시간 테이블 생성
            Statistics newStatistics = statisticsService.findByDate(LocalDate.now());
            newStatistics.setTotalVisitor(1);

            firstVisitWithAuth(findUser, genre, newStatistics);

            statisticsRepository.save(newStatistics);

        } else if (statisticsRepository.findByDate(LocalDate.now()) != null && authentication.getName().equals("anonymousUser")) { // 첫 방문자가 아니면서 로그인 하지 않은 상태

            Statistics statistics = statisticsService.findByDate(LocalDate.now());
            statistics.setTotalVisitor(statistics.getTotalVisitor() + 1);
            statisticsRepository.save(statistics);

        } else if (statisticsRepository.findByDate(LocalDate.now()) != null && !authentication.getName().equals("anonymousUser")) { // 첫 방문자가 아니면서 로그인 한 상태

            User findUser = userService.getLoginUser();
            List<String> genre = userService.getAllGenre(findUser);
            Statistics statistics = statisticsService.findByDate(LocalDate.now());
            statistics.setTotalVisitor(statistics.getTotalVisitor() + 1);

            notFirstVisitWithAuth(findUser, genre, statistics);

            statisticsRepository.save(statistics);

        }
    }

    @AfterReturning(value = "execution(* seb40_main_012.back.config.auth.jwt.JwtTokenizer.delegateRefreshToken(..)) && args(user)", returning = "refreshToken")
    public void signInStatistics(JoinPoint joinPoint, User user, String refreshToken) { // 로그인 하는 경우

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = req.getRemoteAddr();
        ;

        StayTime newSignIn = StayTime.builder()
                .signIn(LocalDateTime.now())
                .user(user)
                .refreshToken(refreshToken)
                .build();

        stayTimeRepository.save(newSignIn);
    }

//    @After(value = "execution(* seb40_main_012.back.config.auth.jwt.JwtTokenizer.removeRefreshToken(..)) && args(tokenValue)")
//    public void signOutStatistics(JoinPoint joinPoint, String tokenValue) { // 로그아웃 한 경우
//
//        String userEmail = refreshTokenRepository.findUserEmailByToken(tokenValue); // 유저 이메일
//        User user = userService.findUserByEmail(userEmail); // 로그아웃 하는 유저
//
//        StayTime findStayTime = stayTimeRepository.findByToken(tokenValue);
//        long duration = Duration.between(findStayTime.getSignIn(), LocalDateTime.now()).getSeconds(); // 로그인 - 로그아웃 간격(초)
//        String durationForStat = duration / 60 + "분 " + duration % 60 + "초";
//
//        if (statisticsRepository.findByDate(LocalDate.now()) == null && findStayTime.getSignIn().getDayOfMonth() != LocalDateTime.now().getDayOfMonth()) { // 전날 로그인 해서 오늘 처음으로 로그아웃 하는 경우
//
//            Statistics newStatistics = Statistics.builder()
//                    .date(LocalDate.now())
//                    .averageStayTimeSec(duration)
//                    .averageStayTimeStr(durationForStat)
//                    .build();
//
//            statisticsRepository.save(newStatistics);
//
//            stayTimeRepository.deleteByLocalDate(LocalDate.now().minusDays(1)); // 하루 전 로그아웃 자료 날리기
//            stayTimeRepository.deleteByLocalDate(LocalDate.now().minusDays(2)); // 이틀 전 로그아웃 자료 날리기
//
//        } else if (statisticsRepository.findByDate(LocalDate.now()) != null) { // 오늘 첫 로그아웃이 아닐 경우
//
//            Statistics statistics = statisticsRepository.findByDate(LocalDate.now()); // 오늘의 통계 객체 불러오기
//
//            long signOutNumToday = stayTimeRepository.findByLocalDate(LocalDate.now()).size(); // 오늘 로그아웃 한 총 사용자(본인 포함)
//
//            long durationBefore = statistics.getAverageStayTimeSec(); // 기존 평균 체류시간
//
//            long durationAfter = (durationBefore * (signOutNumToday - 1) + duration) / signOutNumToday; // 수정된 평균 체류시간
//
//            statistics.setAverageStayTimeSec(durationAfter); // 어제의 로그아웃 기록이 포함된 자료 지우기
//            statistics.setAverageStayTimeStr(durationForStat); // 그저께의 로그아웃 기록이 포함된 자료 지우기
//
//            statisticsRepository.save(statistics);
//
//        }
//
//        stayTimeRepository.deleteByToken(tokenValue);
//    }


    public void firstVisitWithAuth(User findUser, List<String> genre, Statistics newStatistics) {

        if (findUser.getGender() == GenderType.MALE) {

            newStatistics.setMale(1);

            if (genre.contains("NOVEL")) newStatistics.setNovel(1);
            if (genre.contains("ESSAY")) newStatistics.setEssay(1);
            if (genre.contains("POEM")) newStatistics.setPoem(1);
            if (genre.contains("ART")) newStatistics.setArt(1);
            if (genre.contains("HUMANITIES")) newStatistics.setHumanities(1);
            if (genre.contains("SOCIAL")) newStatistics.setSocial(1);
            if (genre.contains("NATURAL")) newStatistics.setScience(1);
            if (genre.contains("COMICS")) newStatistics.setComics(1);
            if (genre.contains("ETC")) newStatistics.setEtc(1);

            switch (findUser.getAge()) {
                case TEENAGER:
                    newStatistics.setTeenager(1);
                case TWENTIES:
                    newStatistics.setTwenties(1);
                case THIRTIES:
                    newStatistics.setThirties(1);
                case FORTIES:
                    newStatistics.setForties(1);
                case FIFTIES:
                    newStatistics.setFifties(1);
                case SIXTIES:
                    newStatistics.setSixties(1);
                case SEVENTIES:
                    newStatistics.setSeventies(1);
                case OTHERS:
                    newStatistics.setOtherGender(1);
            }

        } else if (findUser.getGender() == GenderType.FEMALE) {

            newStatistics.setFemale(1);

            if (genre.contains("NOVEL")) newStatistics.setNovel(1);
            if (genre.contains("ESSAY")) newStatistics.setEssay(1);
            if (genre.contains("POEM")) newStatistics.setPoem(1);
            if (genre.contains("ART")) newStatistics.setArt(1);
            if (genre.contains("HUMANITIES")) newStatistics.setHumanities(1);
            if (genre.contains("SOCIAL")) newStatistics.setSocial(1);
            if (genre.contains("NATURAL")) newStatistics.setScience(1);
            if (genre.contains("COMICS")) newStatistics.setComics(1);
            if (genre.contains("ETC")) newStatistics.setEtc(1);

            switch (findUser.getAge()) {
                case TEENAGER:
                    newStatistics.setTeenager(1);
                    break;
                case TWENTIES:
                    newStatistics.setTwenties(1);
                    break;
                case THIRTIES:
                    newStatistics.setThirties(1);
                    break;
                case FORTIES:
                    newStatistics.setForties(1);
                    break;
                case FIFTIES:
                    newStatistics.setFifties(1);
                    break;
                case SIXTIES:
                    newStatistics.setSixties(1);
                    break;
                case SEVENTIES:
                    newStatistics.setSeventies(1);
                    break;
                case OTHERS:
                    newStatistics.setOtherGender(1);
                    break;
            }

        } else if (findUser.getGender() == GenderType.MYSTIC) {

            newStatistics.setOtherGender(1);

            if (genre.contains("NOVEL")) newStatistics.setNovel(1);
            if (genre.contains("ESSAY")) newStatistics.setEssay(1);
            if (genre.contains("POEM")) newStatistics.setPoem(1);
            if (genre.contains("ART")) newStatistics.setArt(1);
            if (genre.contains("HUMANITIES")) newStatistics.setHumanities(1);
            if (genre.contains("SOCIAL")) newStatistics.setSocial(1);
            if (genre.contains("NATURAL")) newStatistics.setScience(1);
            if (genre.contains("COMICS")) newStatistics.setComics(1);
            if (genre.contains("ETC")) newStatistics.setEtc(1);

            switch (findUser.getAge()) {
                case TEENAGER:
                    newStatistics.setTeenager(1);
                    break;
                case TWENTIES:
                    newStatistics.setTwenties(1);
                    break;
                case THIRTIES:
                    newStatistics.setThirties(1);
                    break;
                case FORTIES:
                    newStatistics.setForties(1);
                    break;
                case FIFTIES:
                    newStatistics.setFifties(1);
                    break;
                case SIXTIES:
                    newStatistics.setSixties(1);
                    break;
                case SEVENTIES:
                    newStatistics.setSeventies(1);
                    break;
                case OTHERS:
                    newStatistics.setOtherGender(1);
                    break;
            }

        } else if (findUser.getGender() == GenderType.NOBODY) {

            newStatistics.setNobody(1);

            if (genre.contains("NOVEL")) newStatistics.setNovel(1);
            if (genre.contains("ESSAY")) newStatistics.setEssay(1);
            if (genre.contains("POEM")) newStatistics.setPoem(1);
            if (genre.contains("ART")) newStatistics.setArt(1);
            if (genre.contains("HUMANITIES")) newStatistics.setHumanities(1);
            if (genre.contains("SOCIAL")) newStatistics.setSocial(1);
            if (genre.contains("NATURAL")) newStatistics.setScience(1);
            if (genre.contains("COMICS")) newStatistics.setComics(1);
            if (genre.contains("ETC")) newStatistics.setEtc(1);

            switch (findUser.getAge()) {
                case TEENAGER:
                    newStatistics.setTeenager(1);
                    break;
                case TWENTIES:
                    newStatistics.setTwenties(1);
                    break;
                case THIRTIES:
                    newStatistics.setThirties(1);
                    break;
                case FORTIES:
                    newStatistics.setForties(1);
                    break;
                case FIFTIES:
                    newStatistics.setFifties(1);
                    break;
                case SIXTIES:
                    newStatistics.setSixties(1);
                    break;
                case SEVENTIES:
                    newStatistics.setSeventies(1);
                    break;
                case OTHERS:
                    newStatistics.setOtherGender(1);
                    break;
            }
        }
    }

    public void notFirstVisitWithAuth(User findUser, List<String> genre, Statistics newStatistics) {

        if (findUser.getGender() == GenderType.MALE) {

            newStatistics.setMale(newStatistics.getMale() + 1);

            if (genre.contains("NOVEL")) newStatistics.setNovel(newStatistics.getNovel() + 1);
            if (genre.contains("ESSAY")) newStatistics.setEssay(newStatistics.getEssay() + 1);
            if (genre.contains("POEM")) newStatistics.setPoem(newStatistics.getPoem() + 1);
            if (genre.contains("ART")) newStatistics.setArt(newStatistics.getArt() + 1);
            if (genre.contains("HUMANITIES")) newStatistics.setHumanities(newStatistics.getHumanities() + 1);
            if (genre.contains("SOCIAL")) newStatistics.setSocial(newStatistics.getSocial() + 1);
            if (genre.contains("NATURAL")) newStatistics.setScience(newStatistics.getScience() + 1);
            if (genre.contains("COMICS")) newStatistics.setComics(newStatistics.getComics() + 1);
            if (genre.contains("ETC")) newStatistics.setEtc(newStatistics.getEtc() + 1);

            switch (findUser.getAge()) {
                case TEENAGER:
                    newStatistics.setTeenager(newStatistics.getTeenager() + 1);
                    break;
                case TWENTIES:
                    newStatistics.setTwenties(newStatistics.getTwenties() + 1);
                    break;
                case THIRTIES:
                    newStatistics.setThirties(newStatistics.getThirties() + 1);
                    break;
                case FORTIES:
                    newStatistics.setForties(newStatistics.getForties() + 1);
                    break;
                case FIFTIES:
                    newStatistics.setFifties(newStatistics.getFifties() + 1);
                    break;
                case SIXTIES:
                    newStatistics.setSixties(newStatistics.getSixties() + 1);
                    break;
                case SEVENTIES:
                    newStatistics.setSeventies(newStatistics.getSeventies() + 1);
                    break;
                case OTHERS:
                    newStatistics.setOtherGender(newStatistics.getOtherGender() + 1);
                    break;
            }
        } else if (findUser.getGender() == GenderType.FEMALE) {

            newStatistics.setFemale(newStatistics.getFemale() + 1);

            if (genre.contains("NOVEL")) newStatistics.setNovel(newStatistics.getNovel() + 1);
            if (genre.contains("ESSAY")) newStatistics.setEssay(newStatistics.getEssay() + 1);
            if (genre.contains("POEM")) newStatistics.setPoem(newStatistics.getPoem() + 1);
            if (genre.contains("ART")) newStatistics.setArt(newStatistics.getArt() + 1);
            if (genre.contains("HUMANITIES")) newStatistics.setHumanities(newStatistics.getHumanities() + 1);
            if (genre.contains("SOCIAL")) newStatistics.setSocial(newStatistics.getSocial() + 1);
            if (genre.contains("NATURAL")) newStatistics.setScience(newStatistics.getScience() + 1);
            if (genre.contains("COMICS")) newStatistics.setComics(newStatistics.getComics() + 1);
            if (genre.contains("ETC")) newStatistics.setEtc(newStatistics.getEtc() + 1);

            switch (findUser.getAge()) {
                case TEENAGER:
                    newStatistics.setTeenager(newStatistics.getTeenager() + 1);
                    break;
                case TWENTIES:
                    newStatistics.setTwenties(newStatistics.getTwenties() + 1);
                    break;
                case THIRTIES:
                    newStatistics.setThirties(newStatistics.getThirties() + 1);
                    break;
                case FORTIES:
                    newStatistics.setForties(newStatistics.getForties() + 1);
                    break;
                case FIFTIES:
                    newStatistics.setFifties(newStatistics.getFifties() + 1);
                    break;
                case SIXTIES:
                    newStatistics.setSixties(newStatistics.getSixties() + 1);
                    break;
                case SEVENTIES:
                    newStatistics.setSeventies(newStatistics.getSeventies() + 1);
                    break;
                case OTHERS:
                    newStatistics.setOtherGender(newStatistics.getOtherGender() + 1);
                    break;
            }
        } else if (findUser.getGender() == GenderType.MYSTIC) {

            newStatistics.setOtherGender(newStatistics.getOtherGender() + 1);

            if (genre.contains("NOVEL")) newStatistics.setNovel(newStatistics.getNovel() + 1);
            if (genre.contains("ESSAY")) newStatistics.setEssay(newStatistics.getEssay() + 1);
            if (genre.contains("POEM")) newStatistics.setPoem(newStatistics.getPoem() + 1);
            if (genre.contains("ART")) newStatistics.setArt(newStatistics.getArt() + 1);
            if (genre.contains("HUMANITIES")) newStatistics.setHumanities(newStatistics.getHumanities() + 1);
            if (genre.contains("SOCIAL")) newStatistics.setSocial(newStatistics.getSocial() + 1);
            if (genre.contains("NATURAL")) newStatistics.setScience(newStatistics.getScience() + 1);
            if (genre.contains("COMICS")) newStatistics.setComics(newStatistics.getComics() + 1);
            if (genre.contains("ETC")) newStatistics.setEtc(newStatistics.getEtc() + 1);

            switch (findUser.getAge()) {
                case TEENAGER:
                    newStatistics.setTeenager(newStatistics.getTeenager() + 1);
                    break;
                case TWENTIES:
                    newStatistics.setTwenties(newStatistics.getTwenties() + 1);
                    break;
                case THIRTIES:
                    newStatistics.setThirties(newStatistics.getThirties() + 1);
                    break;
                case FORTIES:
                    newStatistics.setForties(newStatistics.getForties() + 1);
                    break;
                case FIFTIES:
                    newStatistics.setFifties(newStatistics.getFifties() + 1);
                    break;
                case SIXTIES:
                    newStatistics.setSixties(newStatistics.getSixties() + 1);
                    break;
                case SEVENTIES:
                    newStatistics.setSeventies(newStatistics.getSeventies() + 1);
                    break;
                case OTHERS:
                    newStatistics.setOtherGender(newStatistics.getOtherGender() + 1);
                    break;
            }
        } else if (findUser.getGender() == GenderType.NOBODY) {

            newStatistics.setNobody(newStatistics.getNobody() + 1);

            if (genre.contains("NOVEL")) newStatistics.setNovel(newStatistics.getNovel() + 1);
            if (genre.contains("ESSAY")) newStatistics.setEssay(newStatistics.getEssay() + 1);
            if (genre.contains("POEM")) newStatistics.setPoem(newStatistics.getPoem() + 1);
            if (genre.contains("ART")) newStatistics.setArt(newStatistics.getArt() + 1);
            if (genre.contains("HUMANITIES")) newStatistics.setHumanities(newStatistics.getHumanities() + 1);
            if (genre.contains("SOCIAL")) newStatistics.setSocial(newStatistics.getSocial() + 1);
            if (genre.contains("NATURAL")) newStatistics.setScience(newStatistics.getScience() + 1);
            if (genre.contains("COMICS")) newStatistics.setComics(newStatistics.getComics() + 1);
            if (genre.contains("ETC")) newStatistics.setEtc(newStatistics.getEtc() + 1);

            switch (findUser.getAge()) {
                case TEENAGER:
                    newStatistics.setTeenager(newStatistics.getTeenager() + 1);
                    break;
                case TWENTIES:
                    newStatistics.setTwenties(newStatistics.getTwenties() + 1);
                    break;
                case THIRTIES:
                    newStatistics.setThirties(newStatistics.getThirties() + 1);
                    break;
                case FORTIES:
                    newStatistics.setForties(newStatistics.getForties() + 1);
                    break;
                case FIFTIES:
                    newStatistics.setFifties(newStatistics.getFifties() + 1);
                    break;
                case SIXTIES:
                    newStatistics.setSixties(newStatistics.getSixties() + 1);
                    break;
                case SEVENTIES:
                    newStatistics.setSeventies(newStatistics.getSeventies() + 1);
                    break;
                case OTHERS:
                    newStatistics.setOtherGender(newStatistics.getOtherGender() + 1);
                    break;
            }
        }
    }

    @WebListener
    public static class sessionStatistics implements HttpSessionListener { // 세션 사용하게 되면 쓰기

        @Override
        public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        }

        @Override
        public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        }
    }

    @AfterReturning(value = "execution(* seb40_main_012.back.user.controller.UserController.emailConfirm(..)) && args(emailDto))", returning = "response")
    public void sendConfirmEmail(JoinPoint joinPoint, UserDto.EmailDto emailDto, String response) { // 이메일 인증

        try {
            emailSenderService.sendAuthCode(emailDto.getEmail(), response);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

}
