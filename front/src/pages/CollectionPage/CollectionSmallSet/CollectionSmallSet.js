import styled from 'styled-components';
import CollectionSetTitle from '../CollectionSetTitle';
import CollectionSmallBooks from './CollectionSmallBooks';
import Slider from 'react-slick';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';
import { NextArrow, PrevArrow } from '../../../components/CarouselArrows';

const SlickSlider = styled.div`
  width: 100%;
  //기본 arrow 삭제
  .slick-prev::before,
  .slick-next::before {
    opacity: 0;
    display: none;
  }
  .slick-next {
    right: 20px;
    top: 47%;
  }
  .slick-prev {
    left: 22px;
    top: 47%;
    z-index: 100;
  }
`;

const CollectionSmallSetContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 40px;
  padding: 0 40px;
`;

const CollectionBooks = styled.div`
  display: flex;
  justify-content: center;
`;

const CollectionSmallSet = () => {
  const settings = {
    dots: true,
    infinite: true,
    speed: 500,
    slidesToShow: 4,
    slidesToScroll: 4,
    nextArrow: <NextArrow />,
    prevArrow: <PrevArrow />,
  };

  return (
    <CollectionSmallSetContainer>
      <CollectionSetTitle title="나의 컬렉션" isMyCollection={true} />
      <CollectionBooks>
        <SlickSlider>
          <Slider {...settings}>
            <CollectionSmallBooks title="재밌는 책 컬렉션" />
            <CollectionSmallBooks title="무서운 책 컬렉션" />
            <CollectionSmallBooks title="감동적인 책 컬렉션" />
            <CollectionSmallBooks title="설레는 책 컬렉션" />
            <CollectionSmallBooks title="흥미진진한 책 컬렉션" />
            <CollectionSmallBooks title="슬픈 책 컬렉션" />
            <CollectionSmallBooks title="따뜻한 책 컬렉션" />
          </Slider>
        </SlickSlider>
      </CollectionBooks>
    </CollectionSmallSetContainer>
  );
};

export default CollectionSmallSet;
