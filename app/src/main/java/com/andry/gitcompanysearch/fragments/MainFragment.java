package com.andry.gitcompanysearch.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andry.gitcompanysearch.CompanyDetailDownloader;
import com.andry.gitcompanysearch.GitFetch;
import com.andry.gitcompanysearch.Preferences;
import com.andry.gitcompanysearch.R;
import com.andry.gitcompanysearch.model.CompanyItem;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainFragment extends Fragment implements CompanyDetailDownloader.DetailsDownloadedListener<MainFragment.CompanyHolder> {
    private EditText searchText;
    private RecyclerView recyclerView;
    private View emptyView;
    private TextView messageException;
    private ProgressBar progressBar;
    private CompanyAdapter companyAdapter;
    private List<CompanyItem> companies;
    private Handler mainHandler;
    private HashMap<Integer, Boolean> isItemFullyLoadedMap;
    private CompanyDetailDownloader<CompanyHolder> detailDownloader;
    private String query;
    private FetchItemsTask fetchItemsTask;
    /**
     * isLoading - prevents multiple reload of data when bottom of recyclerView is reached.
     */
    private boolean isLoading;
    private int page = 1;
    private int emptyViewInitialHeight;
    private int searchTextCharAmount = 0;
    private int startSearchAmount;
    private CompanySelectListener callback;

    public static Fragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        startSearchAmount = getResources().getInteger(R.integer.amount_of_chars_start_search);
        companies = new ArrayList<>();
        isItemFullyLoadedMap = new HashMap<>();
        setHandlerThread();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        emptyView = view.findViewById(R.id.fragment_main_empty_view);
        emptyViewInitialHeight = getResources().getDimensionPixelSize(R.dimen.empty_view_height);
        progressBar = view.findViewById(R.id.fragment_main_progress);
        messageException = view.findViewById(R.id.fragment_main_exception_message);
        messageException.setVisibility(View.GONE);
        fetchItemsIfPrefExist();
        TextView title = view.findViewById(R.id.fragment_main_title);
        title.setText(R.string.app_name);
        setSearchText(view);
        setRecyclerView(view);
        if (isAdded())
            createAdapter();
        if (searchTextCharAmount >= startSearchAmount)
            animateTitle(emptyViewInitialHeight, 0);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (CompanySelectListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detailDownloader.clearQuery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detailDownloader.quit();
    }

    @Override
    public void onDetailsDownloaded(CompanyItem item, CompanyHolder target, int position) {
        if (item.getName() != null && !companies.isEmpty()) {
            target.bindDetails(item);
            companies.set(position, item);
        }
    }

    private void setHandlerThread() {
        mainHandler = new Handler();
        detailDownloader = new CompanyDetailDownloader<>(mainHandler);
        detailDownloader.setDetailsDownloadedListener(this);
        detailDownloader.start();
        detailDownloader.getLooper();
    }

    private void fetchItemsIfPrefExist() {
        query = Preferences.getQueryPreferences(getActivity());
        fetchItemsTask = new FetchItemsTask();
        if (query != null) {
            executeLoading();
            searchTextCharAmount = query.length();
        } else
            query = "";
    }

    private void executeLoading() {
        progressBar.setVisibility(View.VISIBLE);
        fetchItemsTask.execute();
    }

    private void setSearchText(View view) {
        searchText = view.findViewById(R.id.fragment_main_search);
        searchText.setText(query);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        searchText.setText(query);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                /**
                 * method is also called on configuration changes. Don't reload data if was configuration changes.
                 */
                if (!query.equals(s.toString())) {
                    if (s.length() >= startSearchAmount) {
                        stopFetching();
                        query = s.toString();
                        fetchItemsTask = new FetchItemsTask();
                        cleanData();
                        executeLoading();
                        Preferences.setQueryPreferences(query, getActivity());
                    } else {
                        stopFetching();
                        cleanData();
                        companyAdapter.notifyDataSetChanged();
                        Preferences.setQueryPreferences(null, getActivity());
                        query = "";
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == startSearchAmount - 1 && searchTextCharAmount == startSearchAmount)
                    animateTitle(emptyView.getMeasuredHeight(), emptyViewInitialHeight);
                 else if (s.length() == startSearchAmount && searchTextCharAmount == startSearchAmount - 1)
                    animateTitle(emptyViewInitialHeight, 0);
                searchTextCharAmount = s.length();
            }
        });
    }

    private void animateTitle(int startHeight, int changeHeight) {
        ValueAnimator anim = ValueAnimator.ofInt(startHeight, changeHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = emptyView.getLayoutParams();
                layoutParams.height = val;
                emptyView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(1000);
        anim.start();
    }

    private void setRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.fragment_main_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(View.SCROLL_INDICATOR_BOTTOM) && !isLoading) {
                    isLoading = true;
                    page++;
                    new FetchItemsTask().execute();
                }
            }
        });
    }

    private void stopFetching() {
        if (fetchItemsTask != null)
            fetchItemsTask.cancel(true);
    }

    private void cleanData() {
        companies.clear();
        isItemFullyLoadedMap.clear();
        page = 1;
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<CompanyItem>> {

        private Exception exception;

        @Override
        protected List<CompanyItem> doInBackground(Void... voids) {
            try {
                List<CompanyItem> items = new GitFetch().downloadAllCompaniesInfo(query, page);
                return items;
            } catch (JSONException e) {
                exception = new Exception(getResources().getString(R.string.message_exception_limit));
            } catch (IOException e) {
                exception = new Exception(getResources().getString(R.string.message_exception_no_internet));
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<CompanyItem> companyItems) {
            super.onPostExecute(companyItems);
            progressBar.setVisibility(View.GONE);

            if (exception == null) {
                if (companyItems.isEmpty()) {
                    // to prevent reloading on swipe if all data downloaded
                    isLoading = true;
                } else {
                    companies.addAll(companyItems);
                    setupAdapter();
                    isLoading = false;
                }
            } else {
                messageException.setText(exception.getMessage());
                messageException.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupAdapter() {
        if (companyAdapter == null) {
            createAdapter();
        } else {
            companyAdapter.notifyDataSetChanged();
        }
    }

    private void createAdapter() {
        companyAdapter = new CompanyAdapter();
        recyclerView.setAdapter(companyAdapter);
    }

    class CompanyHolder extends RecyclerView.ViewHolder {

        private ImageView thumb;
        private TextView title;
        private TextView location;
        private TextView site;
        private CompanyItem item;

        public CompanyHolder(View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.company_item_thumb);
            title = itemView.findViewById(R.id.company_item_title);
            location = itemView.findViewById(R.id.company_item_location);
            site = itemView.findViewById(R.id.company_item_site);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onCompanySelected(item);
                }
            });
        }

        public void bind (CompanyItem item) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(thumb);
            title.setText(item.getName());
            location.setText(item.getLocation());
            site.setText(item.getSite());
            this.item = item;
        }

        public void bindDetails (CompanyItem item) {
            location.setText(item.getLocation());
            site.setText(item.getSite());
            this.item = item;
        }
    }

    private class CompanyAdapter extends RecyclerView.Adapter<CompanyHolder> {

        @NonNull
        @Override
        public CompanyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.company_item, parent, false);
            return new CompanyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CompanyHolder holder, int position) {
            if (isItemFullyLoadedMap.get(position) == null)
                isItemFullyLoadedMap.put(position, false);

            CompanyItem item = companies.get(position);
            if (!isItemFullyLoadedMap.get(position)) {
                detailDownloader.queryCompanyDetails(item, holder, position);
                isItemFullyLoadedMap.put(position, true);
            }
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return companies.size();
        }
    }

    public interface CompanySelectListener {
        void onCompanySelected(CompanyItem item);
    }
}
