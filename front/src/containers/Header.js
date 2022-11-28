import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Divider from '@mui/material/Divider';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import ListItemIcon from '@mui/material/ListItemIcon';
import MenuIcon from '@mui/icons-material/Menu';
import Toolbar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import Avatar from '@mui/material/Avatar';
import IconButton from '@mui/material/IconButton';
import MoreIcon from '@mui/icons-material/MoreVert';
import AccountCircle from '@mui/icons-material/AccountCircle';
import LoginIcon from '@mui/icons-material/Login';
import LogoutIcon from '@mui/icons-material/Logout';
import styled from 'styled-components';
import { Searchbar } from 'components';
import { selectIsLogin, selectProfileImage } from 'store/modules/authSlice';
import { dummyUserImgUrl } from 'util/userAvatar';
import { authApi } from 'api';

const drawerWidth = 240;
const PAIRING = '페어링';
const COLLECTION = '컬렉션';
const navItems = [PAIRING, COLLECTION];

const HeaderContainerStyled = styled(AppBar)`
  height: 60px;
  background-color: white;
  border-bottom: 1px solid ${({ theme }) => theme.colors.lightgray};
  /* z-index: 5050; */
  box-shadow: none;
`;

const ToolbarStyled = styled(Toolbar)`
  min-height: 60px;
`;

const LogoContainerStyled = styled.div`
  margin: 0 40px 0 20px;
`;

const LogoSytled = styled.img`
  padding: 17px 0 10px;
`;

const HeaderBtn = styled(Button)`
  color: ${({ theme }) => theme.colors.dark};
  border: none;
  &:hover {
    background-color: transparent;
  }
`;

const HeaderItemButtonStyled = styled(HeaderBtn)`
  font-size: 16px;
  font-weight: 700;
  margin: 0 15px;
  border-bottom: 3px solid transparent;
  height: 60px;
  border-top: 3px solid transparent;
  &:hover,
  &.selected {
    border-bottom: 3px solid ${({ theme }) => theme.colors.mainColor};
  }
`;

const DrawerListItemButtonStyled = styled(ListItemButton)`
  &.selected {
    background-color: rgba(0, 0, 0, 0.04);
  }
  &:hover {
    background-color: ${({ theme }) => theme.colors.purple_3};
  }
`;

const AuthButtounStyled = styled(HeaderBtn)`
  font-size: 13px;
  font-weight: 400;
  &:hover {
    color: ${({ theme }) => theme.colors.mainColor};
  }
`;

const MyPageIconContainer = styled(Link)`
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 20px 0 10px;
  @media screen and (max-width: 600px) {
    margin-right: 0px;
  }
`;

const logo = (
  <Link to="/">
    <LogoSytled
      src={process.env.PUBLIC_URL + '/images/CherryPick.svg'}
      alt="CherryPick main logo"
    />
  </Link>
);

function Header(props) {
  const navigate = useNavigate();
  const location = useLocation();
  const isLogin = useSelector(selectIsLogin);
  const profileImage = useSelector(selectProfileImage);

  const userAvatar = (
    <Avatar
      alt="Mypage Icon"
      src={profileImage ? profileImage : dummyUserImgUrl}
      sx={{ width: 32, height: 32 }}
    />
  );

  const loginUserItems = [
    { text: '마이페이지', icon: userAvatar },
    { text: '로그아웃', icon: <LogoutIcon /> },
  ];
  const nonMembersItems = [
    { text: '로그인', icon: <LoginIcon /> },
    { text: '회원가입', icon: <AccountCircle /> },
  ];

  const userNavListItem = (arr) =>
    arr.map(({ text, icon }, index) => (
      <ListItem key={text} disablePadding>
        <DrawerListItemButtonStyled
          onClick={handleClickDrawerListItem}
          className={selectedIndex.right === index ? 'selected' : ''}
        >
          <ListItemIcon>{icon}</ListItemIcon>
          <ListItemText primary={text} />
        </DrawerListItemButtonStyled>
      </ListItem>
    ));

  const { window } = props;
  const [drawerOpen, setDrawerOpen] = useState({
    left: false,
    right: false,
  });
  const [selectedIndex, setSelectedIndex] = useState({
    left: null,
    right: null,
  });

  useEffect(() => {
    const { pathname } = location;
    if (pathname.startsWith('/pairing')) {
      setSelectedIndex({ ...selectedIndex, left: navItems.indexOf(PAIRING) });
      return;
    }
    if (pathname.startsWith('/collection')) {
      setSelectedIndex({
        ...selectedIndex,
        left: navItems.indexOf(COLLECTION),
      });
      return;
    }
  }, [location]);

  const handleDrawerToggle = (anchor) => {
    setDrawerOpen((prev) => {
      return { ...prev, [anchor]: !prev[anchor] };
    });
  };

  const container =
    window !== undefined ? () => window().document.body : undefined;

  const handleClickDrawerListItem = (e) => {
    switch (e.target.textContent) {
      case PAIRING:
        navigate('/pairing');
        break;
      case COLLECTION:
        navigate('/collection');
        break;
      case '로그인':
        navigate('/user/signin');
        break;
      case '회원가입':
        navigate('/user/signup');
        break;
      case '마이페이지':
        navigate('/mypage');
        break;
      case '로그아웃':
        handleClickLogoutButton();
        break;
      default:
        break;
    }
  };

  const handleClickNavigateButton = (url) => {
    navigate(url);
  };

  const handleClickLogoutButton = async () => {
    try {
      await authApi.logout();
    } catch (e) {
      console.log(e);
    }
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <HeaderContainerStyled component="nav">
        <ToolbarStyled>
          <IconButton
            aria-label="open drawer"
            edge="start"
            onClick={() => handleDrawerToggle('left')}
            sx={{ mr: 2, display: { md: 'none' }, flex: '0 0 0' }}
          >
            <MenuIcon />
          </IconButton>
          <Box
            sx={{
              display: {
                xs: 'none',
                sm: 'flex',
              },
              flexGrow: 1,
            }}
          >
            <LogoContainerStyled>{logo}</LogoContainerStyled>
            {navItems.map((item, index) => (
              <HeaderItemButtonStyled
                sx={{ display: { sm: 'none', md: 'flex' } }}
                className={selectedIndex.left === index ? 'selected' : ''}
                key={item}
                onClick={handleClickDrawerListItem}
              >
                {item}
              </HeaderItemButtonStyled>
            ))}
          </Box>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flex: { xs: '1 0 auto', sm: '0 1 0' },
            }}
          >
            <Searchbar />
            <Box
              sx={{
                display: { xs: 'none', md: 'flex' },
                alignItems: 'center',
              }}
            >
              {isLogin ? (
                <>
                  <AuthButtounStyled onClick={handleClickLogoutButton}>
                    로그아웃
                  </AuthButtounStyled>
                  <MyPageIconContainer to="/mypage">
                    {userAvatar}
                  </MyPageIconContainer>
                </>
              ) : (
                <>
                  <AuthButtounStyled
                    onClick={() => handleClickNavigateButton('/user/signin')}
                  >
                    로그인
                  </AuthButtounStyled>
                  <AuthButtounStyled
                    onClick={() => handleClickNavigateButton('/user/signup')}
                  >
                    회원가입
                  </AuthButtounStyled>
                </>
              )}
            </Box>
            <IconButton
              size="large"
              aria-label="show more"
              onClick={() => {
                handleDrawerToggle('right');
              }}
              sx={{
                display: { xs: 'block', md: 'none' },
                marginLeft: '16px',
                marginRight: '-12px',
                padding: '8px',
                flex: '0 0 0',
              }}
            >
              {isLogin ? userAvatar : <MoreIcon />}
            </IconButton>
          </Box>
        </ToolbarStyled>
      </HeaderContainerStyled>
      <Box component="nav">
        <Drawer
          container={container}
          variant="temporary"
          open={drawerOpen.left}
          onClose={() => handleDrawerToggle('left')}
          ModalProps={{
            keepMounted: true, // Better open performance on mobile.
          }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
            },
          }}
        >
          <Box
            onClick={() => handleDrawerToggle('left')}
            sx={{ textAlign: 'center' }}
          >
            {logo}
            <Divider />
            <List>
              {navItems.map((item, index) => (
                <ListItem key={item} disablePadding>
                  <DrawerListItemButtonStyled
                    sx={{ textAlign: 'center' }}
                    onClick={handleClickDrawerListItem}
                    className={selectedIndex.left === index ? 'selected' : ''}
                  >
                    <ListItemText primary={item} />
                  </DrawerListItemButtonStyled>
                </ListItem>
              ))}
            </List>
          </Box>
        </Drawer>
        <Drawer
          container={container}
          variant="temporary"
          anchor="right"
          open={drawerOpen.right}
          onClose={() => handleDrawerToggle('right')}
          ModalProps={{
            keepMounted: true, // Better open performance on mobile.
          }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
            },
          }}
        >
          <Box
            onClick={() => handleDrawerToggle('right')}
            sx={{ textAlign: 'center' }}
          >
            <List>
              {isLogin
                ? userNavListItem(loginUserItems)
                : userNavListItem(nonMembersItems)}
            </List>
          </Box>
        </Drawer>
      </Box>
    </Box>
  );
}

export default Header;
