This repository contains the source code for a microservice-based
movie recommendation engine.  The original dataset used as the basis
for the recommendation engine came from this link:

https://towardsdatascience.com/using-cosine-similarity-to-build-a-movie-recommendation-system-ae7f20842599
 
The vectors in microservices/src/main/resources/dataset.csv file
encapsulate the keywords associated with a movie, its genre(s), its
cast, and its director(s). All those values were combined into a
single string and then vectorize using a transformer model (i.e.,
BERT). The individual values in the vectors carry the semantic meaning
of the string, but since they were produced by a deep learning model
there isn’t really a way to tell what each value “means”. However, the
vectors carry the semantic information of that combined string so the
similarity between the meanings of two strings can be calculated by
computing the cosine similarity between vector representations of
movies.
 
