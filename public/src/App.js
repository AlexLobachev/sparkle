import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import MatchPage from './pages/MatchPage';
import ChatPage from './pages/ChatPage';

const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/matches" element={<MatchPage />} />
                <Route path="/chats" element={<ChatPage />} />
            </Routes>
        </Router>
    );
};

export default App;