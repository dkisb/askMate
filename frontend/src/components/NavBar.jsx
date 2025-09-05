import { Link } from 'react-router-dom';

export default function NavBar() {
  return (
    <div className="navbar custom-dark shadow-sm">
      <div className="navbar-start">
        <Link to="/home">
          <button className="btn btn-xs sm:btn-sm md:btn-md lg:btn-lg xl:btn-xl">Home</button>
        </Link>
      </div>
      <div className="navbar-center text-3xl md:text-4xl font-extrabold tracking-tight">
        <h1>AskMate</h1>
      </div>
      <div className="navbar-end">
        <Link to="/">
          <button className="btn btn-xs sm:btn-sm md:btn-md lg:btn-lg xl:btn-xl">Logout</button>
        </Link>
      </div>
    </div>
  );
}
