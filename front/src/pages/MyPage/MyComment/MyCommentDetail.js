import Grid from '@mui/material/Grid';
import styled from 'styled-components';
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined';
import Typography from '@mui/material/Typography';
import axios from '../../../api/axios';
import { useNavigate } from 'react-router-dom';
import NavigateBook from './NavigateBook';
import NavigatePairing from './NavigatePairing';
import NavigateCollection from './NavigateCollection';
import CollectionThumbnail from './CollectionThumbnail';
import Modal from '@mui/material/Modal';
import { useState } from 'react';
import { setOpenSnackbar } from 'store/modules/snackbarSlice';
import { useDispatch } from 'react-redux';

const Remove = styled.div`
  color: #dee2e6;
  font-size: 24px;
  opacity: 0;
  cursor: pointer;
  &:hover {
    color: #6741ff;
  }
  @media screen and (max-width: 870px) {
    display: none !important;
  }
`;
const ItemContainer = styled.div`
  &:hover {
    ${Remove} {
      opacity: 1;
      img {
        width: 17px;
        height: 17px;
        margin-right: 5px;
        margin-top: 8px;
      }
    }
  }
  .move {
    @media screen and (max-width: 750px) {
      width: 100%;
      flex-direction: column;
    }
  }
`;
const ResizePairing = styled.div`
  cursor: pointer;
  box-sizing: inherit;
  width: 92px !important;
  height: 138px !important;
  margin-left: 15px !important;
  margin: 15px !important;

  background-image: url(${(props) => props.img});
  background-repeat: no-repeat;
  background-size: cover;
  margin-right: 0;
  filter: drop-shadow(3px 3px 3px rgb(93 93 93 / 80%));
  display: flex;
  align-items: center;
  justify-content: center;
`;
const BookImg = styled.div`
  cursor: pointer;
  .resize {
    box-sizing: inherit;
    width: 112px !important;
    height: 158px !important;
    padding: 10px !important;
    margin-left: 8px;

    filter: drop-shadow(3px 3px 3px rgb(93 93 93 / 80%));
  }

  .resize-book {
    box-sizing: inherit;
    width: 112px !important;
    height: 158px !important;
    padding: 10px !important;
    margin-left: 8px;
    filter: drop-shadow(3px 3px 3px rgb(93 93 93 / 80%));
  }
  .move-image {
    width: 112px !important;
    height: 158px !important;
    margin: 0 10px;
  }
`;

const ModalBox = styled.div`
  width: 300px;
  height: 150px;
  position: absolute;
  background-color: white;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .info {
    font-weight: 700;
  }
  .container {
    display: flex;
    margin-top: 20px;
  }
  .delete {
    width: 80px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 5px;
    font-size: 14px;
    font-weight: 700;
    background-color: #ffc5c5;
    color: #850000;
    &:hover {
      cursor: pointer;
    }
  }
  .close {
    width: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 5px;
    font-size: 14px;
    font-weight: 700;
    background-color: #e8e8e8;
    &:hover {
      cursor: pointer;
    }
  }
`;

const MyCommentDetail = ({ data, fetchData }) => {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const dispatch = useDispatch();

  const onRemove = (id) => {
    try {
      axios.delete(`/api/comments/${id}/delete`).then(() => fetchData());
    } catch (error) {
      const { message } = error;
      dispatch(
        setOpenSnackbar({
          severity: 'error',
          message: message,
        })
      );
    }
  };

  return (
    <>
      {data ? (
        <>
          <ItemContainer key={data.commentId}>
            <Grid
              container
              className="move"
              item
              xs={12}
              sx={{
                display: 'flex',
                flexDirection: 'row',
              }}
            >
              <Grid item xs={1.8} className="move-image">
                {data.commentType === 'PAIRING' ? (
                  <>
                    {data.pairingCover ? (
                      <ResizePairing
                        onClick={() => {
                          navigate(`/pairing/${data.contentId}`);
                        }}
                        img={data.pairingCover}
                      ></ResizePairing>
                    ) : (
                      <BookImg
                        onClick={() => {
                          navigate(`/pairing/${data.contentId}`);
                        }}
                      >
                        <img
                          className="resize-book"
                          src={data.cover}
                          alt="book thumbnail"
                        ></img>
                      </BookImg>
                    )}
                  </>
                ) : null}

                {data.commentType === 'BOOK_COLLECTION' ? (
                  <>
                    <BookImg
                      onClick={() => {
                        navigate(`/collection/${data.contentId}`);
                      }}
                    >
                      {data.collectionCover !== null ? (
                        <CollectionThumbnail data={data} />
                      ) : null}
                      {data.collectionCover == null ? (
                        <img
                          className="resize"
                          src={
                            data.collectionCover
                              ? data.collectionCover[0]
                              : '/images/collection.png'
                          }
                          alt="book thumbnail"
                        ></img>
                      ) : null}
                    </BookImg>
                  </>
                ) : null}

                {data.commentType === 'BOOK' ? (
                  <BookImg
                    onClick={() => {
                      navigate(`/book/${data.contentId}`);
                    }}
                  >
                    <img
                      className="resize-book"
                      src={data.cover ? data.cover : '/images/book.png'}
                      alt="book thumbnail"
                    ></img>
                  </BookImg>
                ) : null}
              </Grid>

              <Grid item xs={9.9} sx={{ height: '164px', marginBottom: '5px' }}>
                {data.commentType === 'BOOK' ? (
                  <NavigateBook data={data} navigate={navigate} />
                ) : null}
                {data.commentType === 'PAIRING' ? (
                  <NavigatePairing data={data} navigate={navigate} />
                ) : null}
                {data.commentType === 'BOOK_COLLECTION' ? (
                  <NavigateCollection data={data} navigate={navigate} />
                ) : null}
              </Grid>
              <Grid
                item
                xs={0.2}
                sx={{
                  display: 'flex',
                  flexDirection: 'row-reverse',
                }}
              >
                <Remove onClick={handleOpen}>
                  <DeleteOutlinedIcon />
                </Remove>

                <Modal
                  open={open}
                  onClose={handleClose}
                  aria-labelledby="modal-modal-title"
                  aria-describedby="modal-modal-description"
                >
                  <ModalBox>
                    <div className="info">정말 삭제하시겠습니까?</div>
                    <div className="container">
                      <div
                        className="close"
                        role="presentation"
                        onClick={handleClose}
                      >
                        취소
                      </div>
                      <div
                        className="delete"
                        onClick={() => {
                          onRemove(data.commentId);
                        }}
                        role="presentation"
                      >
                        삭제하기
                      </div>
                    </div>
                  </ModalBox>
                </Modal>
              </Grid>
            </Grid>
          </ItemContainer>
        </>
      ) : (
        <Typography
          color="#737373"
          sx={{
            display: 'flex',
            justifyContent: 'center',
            mt: 1,
            mb: 1,
            fontSize: 17,
            fontWeight: 300,
          }}
          variant="body2"
          component={'span'}
        >
          데이터가 없어요
        </Typography>
      )}
    </>
  );
};
export default MyCommentDetail;
