import styled from 'styled-components';
import PageContainer from '../../../components/PageContainer';
import CollectionDetailHeader from '../../CollectionDetailPage/CollectionDetailHeader';
import CollectionHeaderBtns from '../../CollectionDetailPage/CollectionHeaderBtns';
import PairingOriginBook from './PairingOriginBook';

import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  asyncGetOnePairing,
  asyncPairingLike,
  asyncPairingDislike,
  asyncPairingPick,
  asyncPostPairingComment,
  asyncDeletePairingComment,
  asyncEditPairingComment,
  asyncLikePairingComment,
  asyncDisikePairingComment,
} from '../../../store/modules/pairingSlice';
import { selectEmail } from '../../../store/modules/authSlice';
import Comments from '../../../components/Comments/Comments';
import PatchModal from './PatchModal';
import DeleteModal from './DeleteModal';
import { GenterMatcherToKor } from '../../../util/GenreMatcher';
import LikeButton from './LikeButton';
import PickButton from './PickButton';
import CopyUrlButton from './CopyUrlButton';

const BtnStyleBox = styled.div`
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid ${({ theme }) => theme.colors.lightgray};
`;

const EditModeStyleBox = styled.div`
  display: flex;
  button {
    border: none;
    background-color: transparent;
    display: flex;
    justify-content: flex-start;
    align-items: center;
    cursor: pointer;
  }
`;

const OriginBookWrapper = styled.div`
  display: flex;
  flex-direction: column;
  border-bottom: 1px solid ${({ theme }) => theme.colors.lightgray};
`;

const MainBody = styled.div`
  display: flex;
  flex-direction: column;
  border-bottom: 1px solid ${({ theme }) => theme.colors.lightgray};
`;

const InfoTitle = styled.div`
  margin: 10px 0px 0px 20px;
  font-size: 20px;
  color: ${({ theme }) => theme.colors.dark};
  font-weight: 700;
  padding-bottom: 10px;
  display: flex;
  flex-direction: row;
  p {
    color: ${({ theme }) => theme.colors.mainColor};
  }
`;
const InfoContent = styled.div`
  margin: 10px 0px 0px 20px;
  font-size: 15px;
  color: ${({ theme }) => theme.colors.darkgray};
`;

const PairingDetail = () => {
  const dispatch = useDispatch();
  const { pairingId } = useParams();
  const [isMine, setIsMine] = useState(false);

  useEffect(() => {
    dispatch(asyncGetOnePairing(pairingId));
  }, [dispatch]);

  const pairingData = useSelector((state) => state.pairing.data.pairingRes);
  const bookData = useSelector((state) => state.pairing.data.bookRes);

  useEffect(() => {
    setIsMine(userEmail === pairingData.userInformation?.email);
  }, [dispatch, pairingData]);

  const userEmail = useSelector(selectEmail);

  const handlePairingLike = () => {
    dispatch(asyncPairingLike(pairingId));
  };

  const handlePairingDislike = () => {
    dispatch(asyncPairingDislike(pairingId));
  };

  const handlePairingBookmark = () => {
    dispatch(asyncPairingPick(pairingId));
  };

  const handleCommentAdd = (body) => {
    //dispatch - 코멘트 입력
    dispatch(asyncPostPairingComment({ pairingId, body }));
  };

  const handleCommentDelete = (commentId) => {
    //dispatch - 코멘트 삭제
    dispatch(asyncDeletePairingComment(commentId));
  };

  const handleCommentEdit = (commentId, body) => {
    dispatch(asyncEditPairingComment({ commentId, body }));
  };

  const handleCommentLike = (commentId) => {
    dispatch(asyncLikePairingComment(commentId));
  };

  const handleCommentDislike = (commentId) => {
    dispatch(asyncDisikePairingComment(commentId));
  };

  return (
    <PageContainer footer>
      <CollectionDetailHeader
        title={pairingData.title}
        writer={
          pairingData.userInformation && pairingData.userInformation.nickName
        }
        update={new Date(pairingData.modifiedAt).toLocaleDateString()}
      />
      <BtnStyleBox>
        {isMine ? (
          <EditModeStyleBox>
            <PatchModal />
            <DeleteModal deleteId={pairingData.pairingId} />
          </EditModeStyleBox>
        ) : (
          <div></div>
        )}
        <PickButton
          isBookmarked={pairingData.isBookmarked}
          handleBookmark={handlePairingBookmark}
        />
        <LikeButton
          isLiked={pairingData.isLiked}
          LikePlus={handlePairingLike}
          LikeMinus={handlePairingDislike}
        >
          {pairingData.likeCount}
        </LikeButton>
        <CopyUrlButton />
        <CollectionHeaderBtns
          likeCount={pairingData.likeCount}
          userLike={pairingData.isLiked}
          handleCollectionLike={() => {
            console.log('하트 올려!!');
          }}
        />
      </BtnStyleBox>
      <OriginBookWrapper>
        <InfoTitle>How about pairing this book</InfoTitle>
        <PairingOriginBook
          bookTitle={bookData.title}
          author={bookData.author}
          cover={bookData.cover}
          publisher={bookData.publisher}
          year={bookData.pubDate}
          category={GenterMatcherToKor(bookData.genre)}
          rating={bookData.averageRating}
          bookId={pairingData.isbn13}
          disabled={false}
        ></PairingOriginBook>
      </OriginBookWrapper>
      <MainBody>
        <InfoTitle>
          With this&nbsp; <p>{pairingData.pairingCategory}</p>
        </InfoTitle>
        <InfoContent>
          <p>{pairingData.body}</p>
          <a
            href={pairingData.outLinkPath}
            target="_blank"
            rel="noopener noreferrer"
          >
            {pairingData.outLinkPath}
          </a>
        </InfoContent>
      </MainBody>
      <Comments
        commentsData={pairingData.comments}
        commentAdd={handleCommentAdd}
        commentDelete={handleCommentDelete}
        commentEdit={handleCommentEdit}
        commentLike={handleCommentLike}
        commentDisLike={handleCommentDislike}
      />
    </PageContainer>
  );
};

export default PairingDetail;
