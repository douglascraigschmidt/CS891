package edu.vanderbilt.crawler.unused;

//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.view.ActionMode;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import edu.vanderbilt.webcrawler.R;
//import edu.vanderbilt.crawler.model.ui.adapters.MultiSelectAdapter;
//
///**
// * A fragment representing a list of selectable items. The current
// * implementation is restricted to support only an adapter based on Uris
// * although the adapter implementation itself can display those uris in
// * whatever manner it chooses (as strings or as images etc).
// * <p>
// * This fragment will automatically save and restore the displayed list during
// * configuration changes so the controlling activity does not need implement
// * this functionality.
// * <p>
// * Activities containing this fragment MUST implement the {@link
// * OnFragmentListener} interface which is used to notify the activity when the
// * underlying data list changes as a result of ActionMode deletion commands and
// * also to notify the activity when a list item is clicked.
// */
//public class RecyclerViewFragment
//        extends Fragment
//		implements MultiSelectAdapter.OnSelectionListener {
//    private static final String KEY_URL_LIST = "url_list";
//
//    /**
//     * RecyclerView adapter contains and manages a list of selectable items.
//     */
//    private MultiSelectAdapter mAdapter;
//
//    /**
//     * Action mode handler used to manage list item selection state.
//     */
//    private ActionMode mActionMode;
//
//    /**
//     * Reference to the contained recycler view.
//     */
//    private RecyclerView mRecyclerView;
//
//    /**
//     * Activity listen set in onAttach() and cleared in onDetach().
//     */
//    private OnFragmentListener mListener;
//
//    /**
//     * Required constructor definition for all fragments.
//     */
//    public RecyclerViewFragment() {
//    }
//
//    /*
//     * Activity lifecycle methods.
//     */
//
//    @Override
//    public View onCreateView(
//            LayoutInflater inflater,
//            ViewGroup container,
//            Bundle savedInstanceState) {
//
//        // Create the layout view from the XML template.
//        View view = inflater.inflate(
//                R.layout.webview_url_list, container, false);
//
//        // We dynamically add and remove a select all menu item.
//        setHasOptionsMenu(true);
//
//        return view;
//    }
//
//    /**
//     * Lifecycle hook method called when this fragment is attached to an
//     * activity context. This method is called before onCreate().
//     *
//     * @param context Activity context.
//     */
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentListener) {
//            mListener = (OnFragmentListener) context;
//        } else {
//            throw new RuntimeException("Activity must support listen");
//        }
//    }
//
//    /**
//     * Lifecycle hook method called when this fragment detached from an Activity
//     * context.
//     */
//    @Override
//    public void onDetach() {
//        super.onDetach();
//
//        // Release reference to listen.
//        mListener = null;
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        // Save the output URL list in the received bundle.
//        if (mAdapter.getItemCount() > 0) {
//            //noinspection unchecked
//            outState.putParcelableArrayList(KEY_URL_LIST, mAdapter.getItems());
//        }
//
//        // Allow super class to save it's state.
//        super.onSaveInstanceState(outState);
//    }
//
//    /**
//     * Hook method called when all saved state has been restored into the view
//     * hierarchy of the fragment. This method is called after {@link
//     * #onActivityCreated(Bundle)} and before {@link #onStart()}.
//     *
//     * @param savedInstanceState A bundle containing a previously saved state.
//     */
//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//
//        if (savedInstanceState != null) {
//            ArrayList<Uri> urls =
//                    savedInstanceState.getParcelableArrayList(KEY_URL_LIST);
//            if (urls == null) {
//                urls = new ArrayList<>();
//            }
//
//            // Set the adapter to display the restored URL list.
//            //noinspection unchecked
//            mAdapter.setItems(urls);
//        }
//
//        // Allow super class to restore it's state.
//        super.onViewStateRestored(savedInstanceState);
//    }
//
//    /*
//     * Activity lifecycle helper methods.
//     */
//
//    /**
//     * Called by the parent activity to install a desired MultiSelectAdapter
//     * implementation and LayoutManager.
//     */
//    public void initializeViews(
//			MultiSelectAdapter adapter,
//			RecyclerView.LayoutManager layoutManager) {
//
//        if (getView() == null || !(getView() instanceof RecyclerView)) {
//            throw new IllegalStateException(
//                    "Fragment view has not been created");
//        }
//
//        // Get a reference to the layout's RecyclerView which may or may not
//        // be the main fragment view (depending on the XML resource layout).
//        mRecyclerView = (RecyclerView) getView();
//        assert mRecyclerView != null;
//
//        // Use a vertical linear layout manager.
//        mRecyclerView.setLayoutManager(layoutManager);
//
//        // Set the RecyclerView to use the passed adapter.
//        setAdapter(adapter);
//    }
//
//    /**
//     * Sets the RecyclerView adapter to the passed value.
//     *
//     * @param adapter A MultiSelectAdapter implementation.
//     */
//    private void setAdapter(MultiSelectAdapter adapter) {
//        mAdapter = adapter;
//        mRecyclerView.setAdapter(mAdapter);
//    }
//
//    /*
//     * Action event methods.
//     */
//
//    /**
//     * Item click callback invoked by the adapter. If currently in action mode,
//     * toggle the item selection state. Otherwise, forward the event to the
//     * listen to handle.
//     *
//     * @param view     view of clicked item (not used).
//     * @param position adapter position of clicked item.
//     * @param fragment The PagedFragment class in which the item was clicked.
//     */
//    @Override
//    public void onItemClick(View view, int position, Class<? extends Fragment> fragment) {
//        if (isActionModeEnabled()) {
//            selectItem(position);
//        } else if (mListener != null) {
//            mListener.onItemClicked(view, position, fragment);
//        }
//    }
//
//    /**
//     * Hook method called when an item is clicked. Redirects to method that
//     * receives and additional paged fragment class parameter.
//     *
//     * @param view     The clicked item's view.
//     * @param position The clicked item's adapter position.
//     */
//    @Override
//    public void onItemClick(View view, int position) {
//        onItemClick(view, position, Fragment.class);
//    }
//
//    /**
//     * Item long click callback invoked by the adapter. This fragment handles
//     * long clicks by automatically starting ACTION_MODE which is transparent to
//     * the parent activity.
//     */
//    @Override
//    public boolean onItemLongClick(View view, int position) {
//        selectItem(position);
//        return false;
//    }
//
//    /**
//     * Hook method Called when any items are waiting for data. Just pass it on
//     * the activity.
//     *
//     * @param show {@code true} to show progress, {@code false} to hide it.
//     */
//    @Override
//    public void onShowRefresh(boolean show) {
//        if (mListener != null) {
//            mListener.onShowRefresh(show);
//        }
//    }
//
//    /*
//     * Action command helper methods.
//     */
//
//    /**
//     * Toggles an item selection or selects all items if position is -1.
//     *
//     * @param position item adapter position or -1 for all items.
//     */
//    public void selectItem(int position) {
//        // If ths is the first selection, then start action mode.
//        if (mActionMode == null) {
//            if (mListener != null) {
//                // Allow listen the opportunity to prevent action
//                // mode from starting and to update any widgets if
//                // action mode is allowed to start.
//                if (mListener.onActionModeStarting()) {
//                    mActionMode = startActionMode();
//
//                    // Call listen informing them that action mode was
//                    // successfully started.
//                    mListener.onActionModeStarted();
//                }
//            } else {
//                // No listen, so just start action mode.
//                mActionMode = startActionMode();
//            }
//        }
//
//        if (position == -1) {
//            // Select all items.
//            mAdapter.selectItem(-1, true);
//        } else {
//            // Toggle the current item's activated state and
//            // update the acton mode title bar with the new
//            // selection count.
//            mAdapter.toggleSelection(position);
//        }
//
//        // Update ActionMode title to show the number of selections.
//        mActionMode.setTitle("" + mAdapter.getSelectedCount() + " Selected");
//
//        // Cancel action mode if no more items are activated.
//        if (mAdapter.getSelectedCount() == 0) {
//            finishActionMode();
//        }
//    }
//
//    /**
//     * Hook method called when the ActionBar menu is being created. Since we
//     * want to dynamically add and remove a select all menu item in this
//     * fragment, when need to implement this hook or the hook
//     * onPrepareOptionsMenu() will not be called.
//     *
//     * @param menu     The menu that is being created.
//     * @param inflater An inflater to use to load menu resources.
//     */
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_select_all, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    /**
//     * Hook method called just before the menu is displayed. Here we add or
//     * remove a select all menu item depending on the list contents.
//     *
//     * @param menu The menu that is about to be displayed.
//     */
//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        MenuItem item = menu.findItem(R.id.action_select_all);
//        int count = getItemCount();
//        int selectedCount = mAdapter.getSelectedCount();
//
//        // Only show the select all if the list has items and they are
//        // not all already activated.
//        item.setVisible(count > 0 && selectedCount < count);
//
//        // Always call super class method.
//        super.onPrepareOptionsMenu(menu);
//    }
//
//    /*
//     * UI command input hooks and helper methods.
//     */
//
//    /**
//     * This hook is called whenever an item in your options menu is activated.
//     *
//     * @param item The menu item that was activated.
//     * @return boolean Return false to allow normal menu processing to proceed,
//     * true to consume it here.
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//
//        //noinspection SimplifiableIfStatement
//        switch (item.getItemId()) {
//            case R.id.action_select_all:
//                // Start action mode (if not already started)
//                // and activated all items.
//                selectItem(-1);
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    /**
//     * Standard Android framework ActionMode handler to support multiple list
//     * selections and deletions.
//     *
//     * @return A new ActionMode instance.
//     */
//    private ActionMode startActionMode() {
//        // It is very important to only use an app compat action mode or
//        // else the menu attributes app:showAsAction="never" will not work
//        // due to a namespace mismatch.
//        return ((AppCompatActivity) getActivity()).startSupportActionMode(
//                new ActionMode.Callback() {
//                    @Override
//                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                        // NOTE: do not call mode.getMenuInflater() or else
//                        // all menu xml app:showAsAction declarations will be
//                        // ignored(!) because the mode namespace does not use
//                        // the app namespace.
//                        MenuInflater inflater = getActivity().getMenuInflater();
//                        inflater.inflate(R.menu.menu_action_mode, menu);
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onActionItemClicked(
//                            ActionMode mode,
//                            MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.action_delete:
//                                deleteClicked();
//                                return true;
//                            case R.id.action_select_all:
//                                selectItem(-1);
//                                return true;
//                            default:
//                                doneClicked();
//                                return false;
//                        }
//                    }
//
//                    @Override
//                    public void onDestroyActionMode(ActionMode mode) {
//                        doneClicked();
//                    }
//                });
//    }
//
//    /**
//     * Finishes (ends) the currently running action mode handler.
//     */
//    private void finishActionMode() {
//        if (mActionMode != null) {
//            mActionMode.finish();
//            mActionMode = null;
//            mAdapter.selectItem(-1, false);
//
//            // Notify listen that ActionMode has started so that it
//            // has the opportunity to update layout elements to reflect
//            // the mode change.
//            if (mListener != null) {
//                mListener.onActionModeFinished();
//            }
//        }
//    }
//
//    /**
//     * Hook called by action mode handler to remove activated items.
//     */
//    private void deleteClicked() {
//        //noinspection unchecked
//        mAdapter.removeItems(mAdapter.getSelectedItemsPositions());
//        // Notify listen that ActionMode has caused deletion of
//        // adapter items.
//        if (mListener != null) {
//            mListener.onDataChanged(mAdapter);
//        }
//        finishActionMode();
//    }
//
//    /**
//     * Hook called by action mode handler to remove activated items.
//     */
//    private void doneClicked() {
//        mAdapter.selectItem(-1, false);
//        finishActionMode();
//    }
//
//    /**
//     * Returns the contents of the adapter as string ArrayList.
//     *
//     * @return Current list of displayed strings.
//     */
//    public ArrayList<Uri> getItems() {
//        //noinspection unchecked
//        return mAdapter.getItems();
//    }
//
//    /**
//     * Sets the adapter to use the passed ArrayList of strings.
//     *
//     * @param arrayList An array list of strings.
//     */
//    public void setItems(List<Uri> arrayList) {
//        //noinspection unchecked
//        mAdapter.setItems(arrayList);
//    }
//
//    /**
//     * Adds the passed items (strings) to the end of the displayed list.
//     *
//     * @param uris vararg style array of strings to add.
//     */
//    public void addItems(List<Uri> uris) {
//        //noinspection unchecked
//        mAdapter.addAll(uris);
//    }
//
//    /**
//     * Adds the passed items (strings) to the end of the displayed list.
//     *
//     * @param uris vararg style array of strings to add.
//     */
//    public void addItems(Uri... uris) {
//        //noinspection unchecked
//        // TODO: mAdapter.addAll(new ArrayList<Uri>(uris));
//    }
//
//    /**
//     * Helper method to support refreshing the entire list. This is accomplished
//     * by saving the currently displayed contents before telling the adapter to
//     * remove its contents. The saved list is returned to the caller so that it
//     * can used to reinitialize the adapter in a setItems() call.
//     */
//    public void deleteAllItems() {
//        mAdapter.clear();
//        mListener.onDataChanged(mAdapter);
//    }
//
//    /**
//     * Returns true if this fragment has currently started action mode.
//     *
//     * @return {@code true} if action mode is on; {@code false} if not.
//     */
//    public boolean isActionModeEnabled() {
//        return mActionMode != null;
//    }
//
//    /**
//     * Helper to return the number of items in the list adapter.
//     *
//     * @return The number of displayed items.
//     */
//    public int getItemCount() {
//        return mAdapter.getItemCount();
//    }
//
//    /**
//     * Forward refresh request to adapter.
//     */
//    public void refresh() {
//        //TODO: DownloadManager.clearCache(mAdapter.getClass().getSimpleName());
//        mAdapter.notifyDataSetChanged();
//    }
//
//    /**
//     * Interface required by parent activity context to implement. The listen is
//     * set and unset int onAttach() and onDetach().
//     */
//    public interface OnFragmentListener {
//        /**
//         * Called when this fragment is about to begin ActionMode. Allows
//         * activity to prevent action mode from starting. Views can be updated
//         * to reflect the fact that ActionMode is starting, but any calls to
//         * isActionModeStarted() will return false until onActionMOdeStarted()
//         * is called.
//         *
//         * @return {@code true} to allow action mode; {@code false} to prevent
//         * action mode.
//         */
//        @SuppressWarnings("SameReturnValue")
//        boolean onActionModeStarting();
//
//        /**
//         * Called when ActionMode has started. If your view updating uses the
//         * isActionModeRunning() helper method to determine how views should be
//         * updated, then this is the hook to use to update your views.
//         */
//        void onActionModeStarted();
//
//        /**
//         * Called when ActionMode has ended or has been cancelled.
//         */
//        void onActionModeFinished();
//
//        /**
//         * Called when an ActionMode action has caused a change in the
//         * underlying adapter data content (currently only deletion).
//         *
//         * @param adapter The current fragment adapter.
//         */
//        @SuppressWarnings("UnusedParameters")
//        void onDataChanged(MultiSelectAdapter adapter);
//
//        /**
//         * Called when a single item is clicked (not in ACTION_MODE) so that the
//         * listen can typically being a "details view" operation.
//         *
//         * @param view     Item view.
//         * @param position Item adapter position.
//         */
//        void onItemClicked(View view, int position);
//
//        /**
//         * Called when a single item is clicked (not in ACTION_MODE) so that the
//         * listen can typically being a "details view" operation.
//         *
//         * @param view     Item view.
//         * @param position Item adapter position.
//         * @param fragment The PagedFragment class in which the item was clicked.
//         */
//        void onItemClicked(
//				View view,
//				int position,
//				Class<? extends Fragment> fragment);
//
//        /**
//         * Called from adapter when items are being pending loads.
//         */
//        void onShowRefresh(boolean show);
//    }
//}
