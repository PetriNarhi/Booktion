const searchInput = document.getElementById('searchInput');
const movieResults = document.getElementById('movieResults');
const bookResults = document.getElementById('bookResults');

const movieVoteCountDisplay = document.getElementById('movieVoteCountDisplay');
const bookVoteCountDisplay = document.getElementById('bookVoteCountDisplay');
const btnmovie = document.getElementById('btnmovie');
const btnbook = document.getElementById('btnbook');
let lastSearchedMovie = '';
let lastSearchedBook = '';

async function getVotes(title, type){
    if (!title) return 0;
    const res = await fetch(`/votes?query=${encodeURIComponent(title)}&type=${type}`);
    const data = await res.json();
    return type === 'movie' ? data.movieVotes : data.bookVotes;
}

// This sends the votes guys!
async function sendVote(type, title){
    const res = await fetch(`/vote`, {
      method: 'POST',
        headers:{'Content-Type': 'application/x-www-form-urlencoded'},
       body: `query=${encodeURIComponent(title)}&type=${type}` });

    return await res.json();
}
// When pressing the enter key for a search!
searchInput.addEventListener('keydown', async function (event) {

    if (event.key === 'Enter'){
        event.preventDefault();
        const query = searchInput.value.trim();

        if (!query) return;

        movieResults.textContent = 'Searching movie results ...';
        bookResults.textContent = 'Searching book results ...';
        try{
            const res = await fetch(`/search?query=${encodeURIComponent(query)}`);
            const data = await res.json();

            if (data.movie) {
                lastSearchedMovie = data.movie.title || query;
                movieResults.innerHTML = `
                    <div>
                        <strong> Title:</strong> ${data.movie.title || '-'}<br>
                        <strong> Director:</strong> ${data.movie.director || '-'}<br>
                        <strong> Plot:</strong> ${data.movie.plot ||  '-'}<br>
                        <strong> Year:</strong> ${data.movie.year || '-'}<br>
                        ${data.movie.poster && data.movie.poster !== "N/A" ?
                        `<img src="${data.movie.poster}" alt="Movie poster" style="max-width:150px;margin-top:1em;">` : ''}
                    </div>
                `;

                const votes = await getVotes(lastSearchedMovie, 'movie');
                movieVoteCountDisplay.textContent = votes;
            } else{
                lastSearchedMovie = '';
                movieResults.textContent = 'No movie found.' ;
                movieVoteCountDisplay.textContent = '0';

            }

            if(data.book){
                lastSearchedBook = data.book.title || query;
                bookResults.innerHTML = `
                    <div>
                        <strong> Title:</strong> ${data.book.title || '-'}<br>
                        <strong> Authors:</strong> ${data.book.authors ? data.book.authors.join(", ") : '-'}<br>
                        <strong> Description:</strong> ${data.book.description || '-'}<br>
                        <strong> Published:</strong> ${data.book.publishedDate || '-'}<br>
                        ${data.book.thumbnail && data.book.thumbnail !== "N/A"?
                        `<img src="${data.book.thumbnail}" alt="Book cover" style="max-width:120px;margin-top:1em;">`: ''}
                    </div>
                `;

                const votes = await getVotes(lastSearchedBook, 'book');
                bookVoteCountDisplay.textContent = votes;
            } else {
                lastSearchedBook = '';
                bookResults.textContent = 'No book found.';
                bookVoteCountDisplay.textContent = '0';
            }

        } catch(err){
            movieResults.textContent = 'Error fetching movie data.';
            bookResults.textContent = 'Error fetching book data.';

            movieVoteCountDisplay.textContent = '0';
            bookVoteCountDisplay.textContent = '0';

        }
    }
});

// vote buttons
btnmovie.addEventListener('click', async function (){

    if(!lastSearchedMovie) return;

    await sendVote('movie', lastSearchedMovie);
    movieResults.textContent = '' ;
    bookResults.textContent = '';

    movieVoteCountDisplay.textContent = '';
    bookVoteCountDisplay.textContent = '';
    searchInput.value = '' ;

    movieResults.classList.add('blink-green');
    bookResults.classList.add('blink-red');

    setTimeout(() =>{
        movieResults.classList.remove('blink-green');
        bookResults.classList.remove('blink-red');
    }, 500);
    }
);

btnbook.addEventListener('click', async function(){
    if(!lastSearchedBook) return;

    await sendVote('book', lastSearchedBook);
    movieResults.textContent = '';
    bookResults.textContent = '';

    movieVoteCountDisplay.textContent = '';
    bookVoteCountDisplay.textContent = '';
    searchInput.value = '';

    bookResults.classList.add('blink-green');
    movieResults.classList.add('blink-red');

    setTimeout(() =>{
        bookResults.classList.remove('blink-green');
        movieResults.classList.remove('blink-red');

    }, 500);
   }
);




