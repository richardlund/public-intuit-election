**Exercise**

**Question (Design & Data-Structure):**

A council would like to try new way of elections in a city and adopt it to other cities if it is successful. They want us to build a E-Board for managing elections with following capabilities.

1. Citizens can nominate themselves for the elections as contenders.
2. Contender can post their manifesto on the board. Each manifesto may contain a maximum of 3 ideas.
3. Citizens should be able to see the list of contenders and their respective manifesto.
4. Citizen can rate the ideas on the scale of 0 (bad) to 10 (excellent).
5. Board should allow a citizen to delete his/her rating.
6. If the rating is more than 5, then citizen is added as a follower of the contender. 
7. Anytime a contender posts an idea, an email should be sent out (just do a console.log and not the real mail integration) to his/her followers and even to the followers of recipient (if any).
8. A contender is removed from the election if he/she has at least 1 idea which is rated less than 5 by more than 3 voters. 
9. A contender who has maximum sum of avg.ratings / idea is decided as the winner.
    1. Average Rating Per Idea = Sum of all ratings / No of voters.
    2. Final rating = AvgRating of Idea 1 + Avg Rating of Idea 2 + Avg Rating of Idea 3.

10. Build this system considering space and time efficiency along with OOP principles. [User interface is not a must have. Running program in a terminal is sufficient]
11. You should consider writing code that you would be comfortable submitting for a PR. Please state any assumptions or areas you could not complete due to time pressure.
