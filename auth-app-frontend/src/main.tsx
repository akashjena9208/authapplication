
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { BrowserRouter, Route, Routes } from "react-router";
import About from './Pages/About.tsx';
import Login from './Pages/Login.tsx';
import OAuthFailure from './Pages/OAuthFailure.tsx';
import RootLayout from './Pages/RootLayout.tsx';
import Services from './Pages/Services.tsx';
import Signup from './Pages/Signup.tsx';

createRoot(document.getElementById('root')!).render(

  <BrowserRouter>
    <Routes>
      <Route path='/' element={<RootLayout />}>
    </Routes>
  </BrowserRouter>

);