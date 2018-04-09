
Immutable model state representation containing the state of
all beings and palantiri along with the overall model state
and a possible exception.
<p>
This model state represents the Intent component of the
Model-View-Intent (MVI) architectural design pattern designed
by Hans Dorfmann (Uber). In this pattern, the model is responsible
for maintaining all state information. Any changes in the model
state are pushed to presentation layer which then simply displays 
the current state via UI widget components.
<p>
The goal of this architectural design is to reduce accidental
complexity by restricting data and state to flow in a single
direction from the model layer to the view layer. Ideally, the
view maintains no state information whatsoever (not even
shared preferences or saved instance state bundles). This design
simplifies recreating the activity after a device rotation and
also simplifies instrumentation tests which can be easily mocked 
by simply passing mock snapshots to the UI layer via Espresso.
<p>
This model design is based on 4 generic typed interfaces:
<ul>
<li>ModelProvider<T></li>
<li>StateProvider<T></li>
<li>ModelComponent (extends ModelProvider and StateProvider)</li>
</ul>
<p>
The ModelProvider has a single method
<pre>
getModel()
</pre>
which is used to route state change information from the lowest
level class objects (ModelComponents) up through its ancestor
chain to this Model class. When this class receives a routed
state change event, it creates a snapshot of the current model
state that it forwards to the presentation layer which typically
has registered itself as a ModelObserver.
<p>
The StateProvider<T> interface has 4 state setter methods that can be
used to set the state along with an optional message and exception.
It also provides getter methods to acess the the state, message, and exception.
<pre>
T getState()
@Nullable Throwable getException();
@Nullable String getMessage();
void setState(T State)
void setState(T state, @NotNull String message)
void setState(T state, @NotNull Throwable e)
void setState(T state, @Nullable Throwable e, @Nullable String message)

</pre>
The setState(T state) method will automatically route the state change
up to the top level Model (this class) which then forwards
the model snapshot to any registered ModelObservers.
<p>
<p>
<p>
To use this model, an application needs to first determine what
objects require state and what those states will be. To include
an object as part of the model snapshot that is pushed to the
presentation layer, requires adding an state enumerated type
for that object and then have that object class either implement
the ModelComponent interface that extends the  interface (getState() and setState() methods).
<p>
This model currently supports 3 model components, beings,
palantiri, and the simulator itself. All states are defined
as enumerated types and these include:
<ol>
<li>Simulator states (ModelState enum)
<li>Being states (BeingState enum)
<li>Palantiri states (PalantiriState enum)
</ol>
