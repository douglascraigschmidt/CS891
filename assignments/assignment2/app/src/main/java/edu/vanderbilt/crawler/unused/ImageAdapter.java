package edu.vanderbilt.crawler.unused;

//public class ImageAdapter
//		extends MultiSelectAdapter<Uri, ImageAdapter.GridViewHolder> {
//
//	/**
//	 * Constructor.
//	 *
//	 * @param listener A optional OnSelectionListener.
//	 * @param context  The activity context.
//	 */
//	public ImageAdapter(Context context, @Nullable OnSelectionListener listener) {
//		super(context, listener);
//	}
//
//	/**
//	 * Hook method called by framework to create a new custom Holder. This
//	 * is where you perform expensive operations like inflating views.
//	 *
//	 * @param parent   View parent.
//	 * @param viewType Implementation defined type (not used here).
//	 * @return A new GridViewHolder instance.
//	 */
//	@Override
//	public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		View view = LayoutInflater.from(parent.getContext())
//				.inflate(R.layout.item_grid, parent, false);
//
//		return new GridViewHolder(view);
//	}
//
//	/**
//	 * Called to bind you view to the adapter data associated with this view.
//	 *
//	 * @param holder   The GridViewHolder instance to bind
//	 * @param position The adapter position of the data for this view.
//	 */
//	@Override
//	public void onBindViewHolder(GridViewHolder holder, int position) {
//		// Never rely on passed position; always use the real adapter position.
//		final int adapterPosition = holder.getAdapterPosition();
//
//		// Initialize all user action event listeners.
//		initializeListeners(holder.mImageView, adapterPosition);
//
//		// Asynchronously download and display the target URL image.
//
//		Picasso.with(getContext())
//				.load(getItem(position))
//				.placeholder(IMAGE_VIEW_PLACEHOLDER)
//				.resize(140, 140)
//				.tag(getClass().getSimpleName())
//				.into(holder.mImageView);
//
//		// Draw the current view selection state.
//		drawSelectionState(holder.mImageView, position);
//
//		// Set a unique shared element transitionName to supporting return
//		// shared element transition animations.
//		ViewCompat.setTransitionName(
//				holder.mImageView, String.valueOf(adapterPosition));
//	}
//
//	/**
//	 * Installs click on long-click listeners that are then forwarded to the to
//	 * the OnSelectionListener passed into the adapter constructor.
//	 *
//	 * @param view     The clicked view.
//	 * @param position The clicked view's adapter position.
//	 */
//	private void initializeListeners(View view, final int position) {
//		// Redirect all selection handling to
//		// registered click listen (activity).
//		view.setOnClickListener(v -> {
//			if (getOnSelectionListener() != null) {
//				getOnSelectionListener().onItemClick(v, position);
//			}
//		});
//
//		// Redirect all selection handling to
//		// registered click listen (activity).
//		view.setOnLongClickListener(v ->
//				getOnSelectionListener() != null
//						&& getOnSelectionListener().onItemLongClick(v, position));
//	}
//
//	/**
//	 * Helper method to draw the current item's selection state.
//	 *
//	 * @param view     Any view.
//	 * @param position The view's associated adapter position.
//	 */
//	private void drawSelectionState(AspectImageView view, int position) {
//		// Set list item background color based on selection state.
//		if (isItemSelected(position)) {
//			view.setColorFilter(
//					ContextCompat.getColor(
//							view.getContext(),
//							R.color.grid_item_selected_color_filter),
//					PorterDuff.Mode.SRC_ATOP);
//		} else {
//			view.clearColorFilter();
//		}
//	}
//
//	/**
//	 * A custom RecycleView.Holder implementation that contains just one
//	 * AspectImageView.
//	 */
//	public static class GridViewHolder extends RecyclerView.ViewHolder {
//		public final AspectImageView mImageView;
//
//		/**
//		 * Constructor simply stores a reference to the contained AspectImageView.
//		 *
//		 * @param view The item view.
//		 */
//		public GridViewHolder(View view) {
//			super(view);
//			mImageView = (AspectImageView) view.findViewById(R.id.imageView);
//		}
//	}
//}
