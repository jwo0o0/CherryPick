import PageContainer from '../../components/PageContainer';
import PairingTab from './PairingComponents/PairingTab';
import PairingCuration from './PairingComponents/PairingCuration';
import { useSelector, useDispatch } from 'react-redux';
import { useEffect } from 'react';
import {
  asyncGetCuisinePairingLike,
  asyncGetCuisinePairingNewest,
} from '../../store/modules/cuisinePairingSlice';
const PairingCuisine = () => {
  const dispatch = useDispatch();
  useEffect(() => {
    dispatch(asyncGetCuisinePairingLike());
    dispatch(asyncGetCuisinePairingNewest());
  }, [dispatch]);

  const pairingLikeData = useSelector((state) => state.cuisinePairing.likeData);
  const pairingNewestData = useSelector(
    (state) => state.cuisinePairing.newestData
  );
  const titleLike = '큐레이션 제목: Hot Pairing';
  const titleNewest = '큐레이션 제목: New Pairing';
  return (
    <PageContainer footer>
      <PairingTab pathname="/pairing/cuisine" />
      <PairingCuration title={titleLike} pairingData={pairingLikeData} />
      <PairingCuration title={titleNewest} pairingData={pairingNewestData} />
    </PageContainer>
  );
};

export default PairingCuisine;
