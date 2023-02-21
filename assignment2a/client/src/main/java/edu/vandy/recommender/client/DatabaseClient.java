package edu.vandy.recommender.client;

import edu.vandy.recommender.common.Movie;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static edu.vandy.recommender.common.Constants.Service.DATABASE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@code GatewayApplication} and the
 * {@code Database} microservice that it encapsulates.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 *
 * The {@code ComponentScan} annotation instructs Spring to scan for
 * components that are within the "edu.vandy.recommender" package.
 */
@Component
@ComponentScan("edu.vandy.recommender")
public class DatabaseClient {
    /** 
     * This auto-wired field contains all the movies.
     */
    @Autowired
    Map<String, List<Double>> mMovies;

    /**
     * This auto-wired field connects the {@link RecommenderClient} to
     * the {@link DatabaseSyncProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private DatabaseSyncProxy mDatabaseSyncProxy;

    /**
     * This auto-wired field connects the {@link RecommenderClient} to
     * the {@link TimerSyncProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private TimerSyncProxy mTimerSyncProxy;

    /**
     * Print out the {@link Movie} titles.
     */
    private void printMovieTitles(List<Movie> movies,
                                  String heading) {
        System.out.println(heading);
        movies
            .forEach(movie ->
                     System.out.println(movie.id));
    }

    /**
     * @return A {@link List} of {@link Map.Entry} objects sorted by
     *         key (movie title).
     */
    @NotNull
    private List<Map.Entry<String, List<Double>>> sortedMovies() {
        return mMovies
            // Convert the Map into a Set of Entry objects.
            .entrySet()

            // Convert the Set into a Stream.
            .stream()

            // Sort the Stream elements by key (movie title)
            .sorted(Map.Entry.comparingByKey())

            // Convert the Stream into a List.
            .toList();
    }

    public void testGetMoviesSize() {
        var movies = mDatabaseSyncProxy
            .getMovies(DATABASE);

        assertEquals(4801,
                     movies.size());

        // printMovieTitles(movies, "getMovies() output");
    }

    public void testGetMoviesContents() {
        var movies = mDatabaseSyncProxy
            .getMovies(DATABASE);

        assertEquals(4801, mMovies.size());

        int i = 0;
        for (var movie : sortedMovies()) {
            assertEquals(movie.getKey(),
                         movies.get(i).id);
            assertIterableEquals(movie.getValue(),
                                 movies.get(i++).vector);
        }
    }

    public void testSearchMoviesSize() {
        var searchWord = "Night at the Museum";
        var matchingMovies = mDatabaseSyncProxy
            .searchMovies(DATABASE, searchWord);

        assertEquals(3,
                     matchingMovies.size());
        // printMovieTitles(matchingMovies, "searchMovies() output");
    }

    public void testSearchMoviesContents() {
        var searchWord = "Night at the Museum";
        var matchingMovies = mDatabaseSyncProxy
            .searchMovies(DATABASE, searchWord);

        assertEquals(3,
                     matchingMovies.size());

        var iterator =
            sortedMovies().iterator();
        for (Movie matchingMovie : matchingMovies) {
            String matchedMovie = "";
            outer:
            while (iterator.hasNext()) {
                var nextMovie = iterator.next().getKey();
                var nextMovieWords = nextMovie
                    .split("\\P{L}+");
                for (int i = 0; i < nextMovieWords.length; i++) {
                    if (nextMovieWords[i].equals("Night"))
                        for (int j = i + 1; j < nextMovieWords.length; j++)
                            if (nextMovieWords[j].equals("Museum")) {
                                matchedMovie = nextMovie;
                                break outer;
                            }
                }
            }

            assertEquals(matchedMovie,
                         matchingMovie.id);
        }
    }

    public void testSearchMoviesManySize() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        var matchingMovies = mDatabaseSyncProxy
            .searchMovies(DATABASE, watchedMovies);

        assertEquals(91,
                     matchingMovies.size());

        // printMovieTitles(matchingMovies, "searchMoviesMany() output");
    }

    public void testSearchMoviesManyContents() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        var matchingMovies = mDatabaseSyncProxy
            .searchMovies(DATABASE, watchedMovies);

        assertEquals(91,
                     matchingMovies.size());

        // printMovieTitles(matchingMovies, "output");

        var iterator =
            sortedMovies().iterator();
        for (var expected : matchingMovies) {
            String matchingMovie = "";

            outer:
            while (iterator.hasNext()) {
                for (var nextMovieWord : iterator.next().getKey()
                         .split("\\P{L}+")) {
                    if (nextMovieWord.toLowerCase().contains("iron")
                        || nextMovieWord.contains("Steal")
                        || nextMovieWord.equalsIgnoreCase("star")
                        || nextMovieWord.equalsIgnoreCase("war")) {
                        matchingMovie = nextMovieWord;
                        break outer;
                    }
                }
                if (!matchingMovie.equals(""))
                    assertEquals(expected.id,
                                 matchingMovie);
            }
        }
    }
    
    public void testGetMoviesSizeTimed() {
        var movies = mDatabaseSyncProxy
            .getMoviesTimed(DATABASE);

        assertEquals(4801,
                     movies.size());
    }

    public void testSearchMoviesSizeTimed() {
        var searchWord = "Night at the Museum";
        var matchingMovies = mDatabaseSyncProxy
            .searchMoviesTimed(DATABASE, searchWord);

        assertEquals(3,
                     matchingMovies.size());
    }

    public void testSearchMoviesManySizeTimed() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        var matchingMovies = mDatabaseSyncProxy
            .searchMoviesTimed(DATABASE, watchedMovies);

        assertEquals(91,
                     matchingMovies.size());
    }

    /**
     * Test the Movie microservice.
     */
    public void runMoviesTests() {
        // Run all the non-timed Database microservice tests.
        testGetMoviesSize();
        testGetMoviesContents();
        testSearchMoviesSize();
        testSearchMoviesContents();
        testSearchMoviesManySize();
        testSearchMoviesManyContents();

        // Run all the timed Database microservice tests.
        testGetMoviesSizeTimed();
        testSearchMoviesSizeTimed();
        testSearchMoviesManySizeTimed();
    }

    /**
     * Print results of the timed microservice calls.
     */
    public void printSyncTestResults() {
        var timings = mTimerSyncProxy
            .getTimings();

        System.out.println(timings);
    }
}
