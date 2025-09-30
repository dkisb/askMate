import { useEffect, useState } from "react";

function MyQuestions() {
    const [questions, setQuestions] = useState([]);

    useEffect(() => {
        // Fetch user's questions from the API
        async function fetchQuestions() {
            try {
                const token = localStorage.getItem('jwtToken');
                const res = await fetch('/api/user/myquestions', {
                    headers: {
                        'Content-Type': 'application/json',
                        ...(token ? { Authorization: 'Bearer ' + token } : {}),
                    },
                });
                if (!res.ok) throw new Error('Failed to fetch questions');
                const data = await res.json();
                setQuestions(data);
            } catch (err) {
                console.error(err);
            }
        }
        fetchQuestions();
    }, []);


    return (
        <div>
            <h1>My Questions Page</h1>
            {questions ? (questions.map(q => 
            <><h2>{q.title}</h2><p>{q.content}</p>{q.created}</>)) : ("Loading...")}
        </div>
    );
}

export default MyQuestions;