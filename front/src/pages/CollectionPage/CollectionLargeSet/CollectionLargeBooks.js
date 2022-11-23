import styled from 'styled-components';
import CollectionLargeBook from './CollectionLargeBook';

const CollectionLargeBooksContainer = styled.div`
  display: flex;
  margin: 0 15px;
  justify-content: space-between;
`;

const CollectionLargeBooks = () => {
  return (
    <CollectionLargeBooksContainer>
      <CollectionLargeBook bookTitle="책 제목1" />
      <CollectionLargeBook bookTitle="책 제목2" />
      <CollectionLargeBook bookTitle="책 제목3" />
      <CollectionLargeBook bookTitle="책 제목4" />
      <CollectionLargeBook bookTitle="책 제목5" />
      <CollectionLargeBook bookTitle="책 제목6" />
    </CollectionLargeBooksContainer>
  );
};

export default CollectionLargeBooks;
