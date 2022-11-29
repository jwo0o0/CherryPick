import { Suspense, lazy, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { GlobalStyle } from 'styles';
import { authApi } from 'api';
import { selectIsLogin } from 'store/modules/authSlice';
import { ScrollToTop, LoadingComponent } from 'components';

const RoutesComponent = lazy(() => import('components/RoutesComponent'));

const App = () => {
  const isLogin = useSelector(selectIsLogin);

  useEffect(() => {
    if (isLogin) getToken();
  }, []);

  const getToken = async () => {
    try {
      await authApi.refreshToken();
    } catch (e) {
      console.log(e);
      // 에러코드 나오면 처리 필요
      authApi.logout();
    }
  };

  return (
    <BrowserRouter>
      <ScrollToTop />
      <GlobalStyle />
      <Suspense fallback={<LoadingComponent />}>
        <RoutesComponent />
      </Suspense>
    </BrowserRouter>
  );
};

export default App;
