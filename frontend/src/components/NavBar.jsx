import { Link } from 'react-router-dom';

export default function NavBar() {
  return (
    <div className="navbar custom-dark shadow-sm">
      <div className="navbar-start text-3xl md:text-4xl font-extrabold tracking-tight">
        <Link to="/home" replace aria-label="Go to homepage">
          <h1 className="cursor-pointer">AskMate</h1>
        </Link>
      </div>
      <div className="navbar-end">
        <Link to="/">
          <button className="btn btn-xs sm:btn-sm md:btn-md lg:btn-lg xl:btn-xl">Logout</button>
        </Link>
      </div>
    </div>
  );
}