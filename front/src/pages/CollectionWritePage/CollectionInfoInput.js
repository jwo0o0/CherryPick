import styled from 'styled-components';
import { Tag } from '../../components/tag';
import { useState } from 'react';

const CollectionInfoInputContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  padding: 30px 60px;
  @media screen and (max-width: 980px) {
    padding: 20px 40px;
  }
  @media screen and (max-width: 640px) {
    padding: 20px 20px;
  }
  @media screen and (max-width: 500px) {
    padding: 5px 10px;
  }
`;

const TitleInput = styled.input`
  width: 100%;
  padding: 0 25px;
  font-weight: 700;
  border: 1px solid ${({ theme }) => theme.colors.dark};
  border: none;
  margin: 5px 0;
  &:focus {
    outline: none;
  }
  height: 80px;
  font-size: 24px;
  color: ${({ theme }) => theme.colors.dark};
  @media screen and (max-width: 640px) {
    height: 60px;
    padding: 0 20px;
    font-size: 20px;
  }
  @media screen and (max-width: 500px) {
    height: 40px;
    font-size: 16px;
    padding: 0 10px;
  }
`;

const TagInputContainer = styled.div`
  width: 100%;
  margin: 5px 0;
  padding: 10px;
  display: flex;
  flex-wrap: wrap;
  background-color: white;
  @media screen and (max-width: 500px) {
    padding: 5px;
  }
`;

const Tags = styled.div`
  display: flex;
  align-items: center;
  flex-wrap: wrap;
`;

const TagContainer = styled.div`
  div {
    margin: 5px;
    @media screen and (max-width: 500px) {
      margin: 2px;
    }
    &:hover {
      cursor: pointer;
      border: none;
      color: white;
      border: 1px solid ${({ theme }) => theme.colors.mainColor};
      background-color: ${({ theme }) => theme.colors.mainColor};
    }
  }
`;

const TagInput = styled.input`
  border: none;
  margin: 10px 0 10px 10px;
  &:focus {
    outline: none;
  }
  @media screen and (max-width: 500px) {
    margin: 5px 0 5px 5px;
    font-size: 10px;
  }
`;

const TagInfo = styled.div`
  font-size: 12px;
  display: flex;
  div {
    padding: 10px;
  }
  &.hidden {
    display: none;
  }
  &.show {
    display: flex;
  }
  @media screen and (max-width: 500px) {
    font-size: 10px;
  }
`;

const ContentInputContainer = styled.div`
  height: 150px;
`;
const ContentInput = styled.textarea`
  width: 100%;
  height: 100%;
  padding: 20px 25px;
  margin: 5px 0;
  border: 1px solid ${({ theme }) => theme.colors.dark};
  border: none;
  font-size: 15px;
  font-weight: 400;
  font-family: RobotoInCjk, 'Noto Sans KR', 'Apple SD Gothic Neo',
    'Nanum Gothic', 'Malgun Gothic', sans-serif;
  color: ${({ theme }) => theme.colors.dark};
  line-height: 100%;
  &:focus {
    outline: none;
  }
  @media screen and (max-width: 500px) {
    padding: 10px 10px;
    font-size: 10px;
  }
`;

const CollectionInfoInput = ({ data, setData }) => {
  const [newTag, setNewTag] = useState('');
  const [isOnKeyUpTag, setIsOnKeyUp] = useState(false);

  const handleOnChangeNewTag = (e) => {
    setNewTag(e.target.value);
  };

  const handleOnKeyPressTag = (e) => {
    if (e.key === 'Enter' && newTag.length <= 20 && data.tags.length <= 9) {
      setData({ ...data, tags: [...data.tags, newTag] });
      setNewTag('');
    }
  };

  const handleOnFoucusUpTag = () => {
    setIsOnKeyUp(true);
  };
  const handleOnBlurTag = () => {
    setIsOnKeyUp(false);
  };

  const handleOnChangeTitle = (e) => {
    setData({ ...data, title: e.target.value });
  };

  const handleOnChangeContent = (e) => {
    setData({ ...data, content: e.target.value });
  };

  const handleDeleteTag = (tagidx) => {
    setData({ ...data, tags: data.tags.filter((el, idx) => idx !== tagidx) });
  };

  return (
    <CollectionInfoInputContainer>
      <TitleInput
        type="text"
        placeholder="컬렉션 제목"
        maxLength="30"
        onChange={handleOnChangeTitle}
        value={data.title}
      ></TitleInput>
      <TagInputContainer>
        <Tags>
          {data.tags.map((el, idx) => {
            return (
              <TagContainer
                key={idx}
                onClick={() => {
                  handleDeleteTag(idx);
                }}
                role="presentation"
              >
                <Tag>#{el}</Tag>
              </TagContainer>
            );
          })}
        </Tags>
        <TagInput
          type="text"
          placeholder="태그를 입력하세요"
          onChange={handleOnChangeNewTag}
          onKeyPress={handleOnKeyPressTag}
          onFocus={handleOnFoucusUpTag}
          onBlur={handleOnBlurTag}
          value={newTag}
        />
      </TagInputContainer>
      <TagInfo className={isOnKeyUpTag ? 'show' : 'hidden'}>
        <div>
          엔터를 입력하여 태그를 등록할 수 있습니다. (최대 10개, 1개당 20자)
          <br />
          등록된 태그를 클릭하면 삭제됩니다.
        </div>
      </TagInfo>
      <ContentInputContainer>
        <ContentInput
          type="text"
          placeholder="컬렉션을 소개해 보세요."
          maxLength="250"
          onChange={handleOnChangeContent}
          value={data.content}
        />
      </ContentInputContainer>
    </CollectionInfoInputContainer>
  );
};

export default CollectionInfoInput;
