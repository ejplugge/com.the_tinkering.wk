/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.the_tinkering.wk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.GravityCompat;

import com.the_tinkering.wk.R;

import java.util.Arrays;

import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.util.Objects.requireNonNull;

/**
 * A grid layout with a rigid column width.
 *
 * <p>
 *     This is a bit like a heavily restricted and stripped down, but more efficient, GridLayout.
 *     But it will also strictly enforce equal column widths no matter what. You can use it mostly
 *     like a GridLayout, but with the following major differences:
 * </p>
 *
 * <ul>
 *     <li>The layout's own layout width is as big as the layout engine will let it be. wrap_content is not
 *     going to do what you want. If you want to restrict the layout's width, either set an explicit width like
 *     <tt>layout_width="100dp"</tt>, or put the layout inside another container that will restrict its size.</li>
 *
 *     <li>The layout's own height is as large as the sum of all row heights, plus margins and padding.
 *     Restricting the layout's height will not work. You should either make sure yourself that the layout
 *     will fit in its container, or enclose the layout in something that can scroll, such as a ScrollView.</li>
 *
 *     <li>The number of columns is set as a property and is static. Child views have no influence over it,
 *     and any child that tries to span more columns will be trimmed down to the number of columns.</li>
 *
 *     <li>The number of rows is dynamic and is as large as needed to give every child view a place.</li>
 *
 *     <li>Child views can span any number of columns, and/or specify that they must be the first child in their
 *     row, but the row span is fixed to 1.</li>
 *
 *     <li>Child views get a fixed width based on the layout's own width and the number of columns, taking into
 *     account padding and margins. Every column is the same width, and child views are forced to fit that mold.</li>
 *
 *     <li>Child views get as much height as they want. The row's height is the maximum of the child views' heights
 *     in that row, plus margins. Each row is sized independently.</li>
 *
 *     <li>Child views cannot specify margins or rows/columns, only their column span and gravity. All cells get
 *     the same margins on all four sides as specified by the childMargin property. All child views are laid
 *     out automatically, child views cannot specify where in the layout they want to appear.</li>
 *
 *     <li>Layout gravity is only relevant if the child view measures itself smaller than the space it gets
 *     assigned to it.</li>
 * </ul>
 */
public class RigidGridLayout extends ViewGroup {
    private int numColumns = 1;
    private int numRows = 0;
    private int childMargin = 0;
    private boolean gridAssigned = false;
    private int[] rowHeights = new int[0];
    private final Rect container = new Rect();
    private final Rect output = new Rect();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public RigidGridLayout(final Context context) {
        super(context);
        safe(this::init);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public RigidGridLayout(final Context context, final @Nullable AttributeSet attrs) {
        super(context, attrs);
        safe(this::init);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     * @param defStyleAttr the default style
     */
    public RigidGridLayout(final Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        safe(this::init);
    }

    private void init() {
        childMargin = (int) (getContext().getResources().getDisplayMetrics().density * 2);
    }

    /**
     * The number of columns in this layout.
     *
     * @param numColumns the number
     */
    protected final void setNumColumns(final int numColumns) {
        this.numColumns = Math.max(numColumns, 1);
    }

    /**
     * Fixed margins for all child views.
     *
     * @param childMargin the margin in pixels
     */
    protected final void setChildMargin(final int childMargin) {
        this.childMargin = childMargin;
    }

    /**
     * Prepare for measurement and layout. This will assign each child view to a row/column,
     * and determine the total number of rows needed.
     */
    private void assignGrid() {
        // Don't recompute if we already know this information.
        if (gridAssigned) {
            return;
        }

        int currentRow = 0;
        int currentColumn = 0;
        numRows = 0;

        // Iterate over all child views
        final int numViews = getChildCount();
        for (int i=0; i<numViews; i++) {
            final @Nullable View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            // Clone the params in case the user is re-using params instances.
            final LayoutParams params = generateLayoutParams(child.getLayoutParams());
            // Sanitize the columnSpan in the params
            if (params.columnSpan < 0) {
                params.columnSpan = numColumns;
            }
            else if (params.columnSpan == 0) {
                params.columnSpan = 1;
            }
            else if (params.columnSpan > numColumns) {
                params.columnSpan = numColumns;
            }
            // Advance to the next row if needed
            if (currentColumn > 0 && (currentColumn >= numColumns || params.nextRow || currentColumn + params.columnSpan > numColumns)) {
                currentColumn = 0;
                currentRow++;
            }
            // Anchor the child view in place.
            params.columnAnchor = currentColumn;
            params.rowAnchor = currentRow;
            currentColumn += params.columnSpan;
            numRows = currentRow + 1;
            child.setLayoutParams(params);
        }

        // This will later store height measurements for each row
        rowHeights = new int[numRows];

        gridAssigned = true;
    }

    @Override
    protected final void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // This does all of the measurement in two passes.
        safe(() -> {
            setMeasuredDimension(100, 100);
            assignGrid();
            Arrays.fill(rowHeights, 0);

            // The width of the layout itself. If the value is 0 (which happens during early layout phases),
            // just pick some default value and rely on the layout engine to do another measurement pass later,
            // which will give a better hint. Otherwise, just grab the maximum width that the layout engine
            // is willing to give us.
            int width = MeasureSpec.getSize(widthMeasureSpec);
            if (width <= 0) {
                width = 250;
            }

            // Determine the width of each cell in the grid, excluding the layout's padding and the child's margins,
            // but including the child's padding.
            int cellWidth = width - getPaddingLeft() - getPaddingRight() - numColumns * childMargin * 2;
            if (cellWidth < 0) {
                cellWidth = 0;
            }
            cellWidth /= numColumns;

            // First pass, iterate over all child views to discover the height each child view wants to have.
            final int numViews = getChildCount();
            for (int i=0; i<numViews; i++) {
                final @Nullable View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                final LayoutParams params = (LayoutParams) requireNonNull(child.getLayoutParams());

                // Compute the horizontal space available to the child, excluding its own margins, but including
                // the gutter between cells if this spans more than one column.
                final int childSpace = cellWidth * params.columnSpan + childMargin * (params.columnSpan-1) * 2;

                // First pass. We already know the child's width, so we force that, but the height is left free.
                // We size the current row based on the outcome of this first measurement.
                child.measure(
                        MeasureSpec.makeMeasureSpec(childSpace, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                rowHeights[params.rowAnchor] = Math.max(child.getMeasuredHeight(), rowHeights[params.rowAnchor]);
            }

            // Second pass, actually lock the view into the size we need it to have.
            for (int i=0; i<numViews; i++) {
                final @Nullable View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                final LayoutParams params = (LayoutParams) requireNonNull(child.getLayoutParams());

                // Compute the horizontal space available to the child, excluding its own margins, but including
                // the gutter between cells if this spans more than one column.
                final int childSpace = cellWidth * params.columnSpan + childMargin * (params.columnSpan-1) * 2;

                // Final measurement - the child doesn't get to decide anything itself anymore, we just force-feed it
                // what we say it should have.
                child.measure(
                        MeasureSpec.makeMeasureSpec(childSpace, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(rowHeights[params.rowAnchor], MeasureSpec.EXACTLY));
            }

            // Add up the row heights plus padding and margins to find the layout's own size.
            int height = getPaddingTop() + getPaddingBottom() + numRows * childMargin * 2;
            for (final int rowHeight: rowHeights) {
                height += rowHeight;
            }

            setMeasuredDimension(width, height);
        });
    }

    @Override
    protected final void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        // The actual layout phase. The hard part of measuring is done, here we just tell each child where
        // in the layout's canvas it must draw itself. The layout doesn't actually draw anything itself, it
        // only gives the child views a place to draw themselves.
        safe(() -> {
            assignGrid();

            // Same as when measuring, this is the width of each cell excluding the layout's padding and the child's margins.
            final int width = getMeasuredWidth();
            int cellWidth = width - getPaddingLeft() - getPaddingRight() - numColumns * childMargin * 2;
            if (cellWidth < 0) {
                cellWidth = 0;
            }
            cellWidth /= numColumns;

            // Iterate over all child views and dump the layout details on them.
            final int numViews = getChildCount();
            for (int i=0; i<numViews; i++) {
                final @Nullable View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                final LayoutParams params = (LayoutParams) requireNonNull(child.getLayoutParams());

                // Figure out the child's final measured size, and (if needed) clamp it down to the space we're willing to give it.
                // The resulting width/height is normally exactly what we sized it to be, but it may be smaller than the available space,
                // and in that case we use the gravity to place it within its cell(s).
                final int childSpace = cellWidth * params.columnSpan + childMargin * (params.columnSpan-1) * 2;
                int childWidth = child.getMeasuredWidth();
                childWidth = Math.min(childWidth, childSpace);
                int childHeight = child.getMeasuredHeight();
                childHeight = Math.min(childHeight, rowHeights[params.rowAnchor]);

                // Apply the gravity, which computes the offsets we need to take into account to follow the desired gravity.
                // Again, normally this does nothing, but it's here for child views that size themselves smaller than requested.
                container.left = 0;
                container.right = childSpace;
                container.top = 0;
                container.bottom = rowHeights[params.rowAnchor];
                GravityCompat.apply(params.gravity, childWidth, childHeight, container, output, 0);

                // Figure out the exact box where the child may draw itself
                final int childLeft = getPaddingLeft() + params.columnAnchor * (cellWidth + childMargin * 2) + childMargin + output.left;
                final int childRight = childLeft + output.width();
                int childTop = getPaddingTop() + childMargin + output.top;
                for (int j=0; j<params.rowAnchor; j++) {
                    childTop += rowHeights[j] + childMargin * 2;
                }
                final int childBottom = childTop + output.height();

                // And inform the child of that.
                child.layout(childLeft, childTop, childRight, childBottom);
            }
        });
    }

    /*
     * The following 4 methods are mostly intended to help out the layout inflater. Basically, with these
     * methods it's easy to ensure that every child view gets a LayoutParams instance of the right class.
     */

    @Override
    protected final LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
    }

    @Override
    public final LayoutParams generateLayoutParams(final @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return generateDefaultLayoutParams();
        }
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected final LayoutParams generateLayoutParams(final @Nullable ViewGroup.LayoutParams p) {
        if (p == null) {
            return generateDefaultLayoutParams();
        }
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        }
        return new LayoutParams(p);
    }

    @Override
    protected final boolean checkLayoutParams(final @Nullable ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /*
     * Make sure we know when anything changes that would require us to re-layout, such as when we change size ourselves,
     * or when child views are added, removed or changed.
     */

    @Override
    public final void invalidate() {
        super.invalidate();
        gridAssigned = false;
    }

    @Override
    public final void requestLayout() {
        super.requestLayout();
        gridAssigned = false;
    }

    /**
     * Per-child layout information.
     */
    @SuppressWarnings("ClassNameSameAsAncestorName")
    public static class LayoutParams extends ViewGroup.LayoutParams {
        /**
         * The number of columns occupied by this view, -1 for 'whole row'.
         */
        protected int columnSpan;

        /**
         * Force this view to be the first view of a new row.
         */
        @SuppressWarnings("CanBeFinal")
        protected boolean nextRow;

        /**
         * Gravity of a child view within the cell.
         */
        protected int gravity;

        private int rowAnchor = 0;
        private int columnAnchor = 0;

        /**
         * The constructor.
         *
         * @param context Android context
         * @param attrs attribute set
         */
        protected LayoutParams(final Context context, final AttributeSet attrs) {
            super(context, attrs);
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RigidGridLayout_Layout);
            columnSpan = a.getInt(R.styleable.RigidGridLayout_Layout_layout_columnSpan, 1);
            nextRow = a.getBoolean(R.styleable.RigidGridLayout_Layout_layout_nextRow, false);
            gravity = a.getInt(R.styleable.RigidGridLayout_Layout_android_layout_gravity, Gravity.TOP | Gravity.START);
            a.recycle();
        }

        /**
         * The constructor.
         *
         * @param width the width for this view
         * @param height the height for this view
         */
        @SuppressWarnings("SameParameterValue")
        protected LayoutParams(final int width, final int height) {
            super(width, height);
            columnSpan = 1;
            nextRow = false;
            gravity = Gravity.TOP | Gravity.START;
        }

        /**
         * The constructor.
         *
         * @param width the width for this view
         * @param height the height for this view
         * @param columnSpan the number of columns occupied by this view, -1 for 'whole row'
         * @param nextRow force this view to be the first view of a new row
         * @param gravity gravity of a child view within the cell
         */
        @SuppressWarnings("unused")
        public LayoutParams(final int width, final int height, final int columnSpan, final boolean nextRow, final int gravity) {
            super(width, height);
            this.columnSpan = columnSpan;
            this.nextRow = nextRow;
            this.gravity = gravity;
        }

        /**
         * The constructor.
         *
         * @param source the params to copy
         */
        protected LayoutParams(final ViewGroup.LayoutParams source) {
            super(source);
            columnSpan = 1;
            nextRow = false;
            gravity = Gravity.TOP | Gravity.START;
        }

        /**
         * The constructor.
         *
         * @param source the params to copy
         */
        protected LayoutParams(final RigidGridLayout.LayoutParams source) {
            super(source);
            columnSpan = source.columnSpan;
            nextRow = source.nextRow;
            gravity = source.gravity;
        }
    }
}
