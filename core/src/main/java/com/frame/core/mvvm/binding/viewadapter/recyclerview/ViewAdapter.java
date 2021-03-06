package com.frame.core.mvvm.binding.viewadapter.recyclerview;

import android.databinding.BindingAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.frame.core.mvvm.binding.command.BindingCommand;

import java.util.concurrent.TimeUnit;

import io.reactivex.subjects.PublishSubject;


/**
 * Created by goldze on 2017/6/16.
 */
public class ViewAdapter {

    @BindingAdapter("lineManager")
    public static void setLineManager(RecyclerView recyclerView, LineManagers.LineManagerFactory lineManagerFactory) {
        recyclerView.addItemDecoration(lineManagerFactory.create(recyclerView));
    }


    @BindingAdapter(value = {"onScrollChangeCommand", "onScrollStateChangedCommand"}, requireAll = false)
    public static void onScrollChangeCommand(final RecyclerView recyclerView,
                                             final BindingCommand<ScrollDataWrapper> onScrollChangeCommand,
                                             final BindingCommand<Integer> onScrollStateChangedCommand) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int state;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (onScrollChangeCommand != null) {
                    onScrollChangeCommand.execute(new ScrollDataWrapper(dx, dy, state));
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                state = newState;
                if (onScrollStateChangedCommand != null) {
                    onScrollChangeCommand.equals(newState);
                }
            }
        });

    }

    @SuppressWarnings("unchecked")
    @BindingAdapter({"onLoadMoreCommand"})
    public static void onLoadMoreCommand(final RecyclerView recyclerView, final BindingCommand<Integer> onLoadMoreCommand) {
        RecyclerView.OnScrollListener listener = new OnScrollListener(onLoadMoreCommand);
        recyclerView.addOnScrollListener(listener);

    }

    public static class OnScrollListener extends RecyclerView.OnScrollListener {

        private PublishSubject<Integer> methodInvoke = PublishSubject.create();

        private BindingCommand<Integer> onLoadMoreCommand;

        OnScrollListener(final BindingCommand<Integer> onLoadMoreCommand) {
            this.onLoadMoreCommand = onLoadMoreCommand;
            methodInvoke.throttleFirst(1, TimeUnit.SECONDS)
                    .subscribe(onLoadMoreCommand::execute);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            /*LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                if (onLoadMoreCommand != null) {
                    methodInvoke.onNext(recyclerView.getAdapter().getItemCount());
                }
            }*/
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            if (onLoadMoreCommand != null) {
                                methodInvoke.onNext(recyclerView.getAdapter().getItemCount());
                            }
                        }
                    }
                    break;
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    break;
                case RecyclerView.SCROLL_STATE_SETTLING:
                    break;
            }
        }


    }

    public static class ScrollDataWrapper {
        public float scrollX;
        public float scrollY;
        public int state;

        public ScrollDataWrapper(float scrollX, float scrollY, int state) {
            this.scrollX = scrollX;
            this.scrollY = scrollY;
            this.state = state;
        }
    }
}
