

   async function fetchLeaderboard(endpoint, containerId){

        try{
            const response = await fetch(endpoint);
            const  data = await response.json();
            const container = document.getElementById(containerId);
            container.innerHTML = "";
            if(data.length === 0){
                container.innerHTML = "<p>No votes yet.</p>";
                return;
            }

            data.forEach((item, index) =>{
                const div = document.createElement("div");
                div.style.marginBottom = "12px";
                div.innerHTML = `<strong>${index + 1}. ${item.title}</strong> – ${item.votes} votes`;
                container.appendChild(div);
            }
            );

        } catch(error){
            document.getElementById(containerId).innerHTML = "<p>Error loading leaderboard.</p>";

        }
    }


    fetchLeaderboard("/leaderboard/movies", "topMovies");
    fetchLeaderboard("/leaderboard/books", "topBooks");





