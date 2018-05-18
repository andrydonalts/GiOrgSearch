package com.andry.gitcompanysearch.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andry.gitcompanysearch.GitFetch;
import com.andry.gitcompanysearch.R;
import com.andry.gitcompanysearch.model.CompanyItem;
import com.andry.gitcompanysearch.model.Repo;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RepoFragment extends Fragment {

    private static final String COMPANY_ITEM_BUNDLE = "COMPANY_ITEM_BUNDLE";

    private RecyclerView recyclerView;
    private GitFetch gitFetch;
    private CompanyItem companyItem;
    private List<Repo> repos;
    private RepoAdapter repoAdapter;
    private TextView exceptionMessage;
    private int page = 1;
    private boolean isLoading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        companyItem = getArguments().getParcelable(COMPANY_ITEM_BUNDLE);
        repos = new ArrayList<>();
        gitFetch = new GitFetch();
        new FetchReposTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repo, container, false);
        String titleText = getResources().getQuantityString(R.plurals.toolbar_title_format,
                companyItem.getTotalRepos(), companyItem.getName(), companyItem.getTotalRepos());
        String upperCaseTitle = titleText.substring(0, 1).toUpperCase() + titleText.substring(1);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(upperCaseTitle);
        exceptionMessage = view.findViewById(R.id.fragment_repo_exception_message);
        exceptionMessage.setVisibility(View.GONE);
        setRecyclerView(view);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.fragment_repo_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(View.SCROLL_INDICATOR_BOTTOM) && !isLoading) {
                    isLoading = true;
                    page++;
                    new FetchReposTask().execute();
                }
            }
        });
    }

    private class FetchReposTask extends AsyncTask<Void, Void, List<Repo>> {

        private Exception exception;

        @Override
        protected List<Repo> doInBackground(Void... voids) {
            try {
                List<Repo> repos = gitFetch.fetchRepos(companyItem.getName(), page);
                return repos;
            } catch (JSONException e) {
                exception = new Exception(getResources().getString(R.string.message_exception_limit));
            } catch (IOException e) {
                exception = new Exception(getResources().getString(R.string.message_exception_no_internet));
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Repo> jsonRepos) {
            super.onPostExecute(jsonRepos);
            if (exception == null) {
                if (jsonRepos.isEmpty())
                    isLoading = true;
                else
                    repos.addAll(jsonRepos);
                isLoading = false;
                if (repoAdapter == null) {
                    repoAdapter = new RepoAdapter();
                    recyclerView.setAdapter(repoAdapter);
                } else {
                    repoAdapter.notifyDataSetChanged();
                }
            } else {
                exceptionMessage.setText(exception.getMessage());
                exceptionMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private class RepoHolder extends RecyclerView.ViewHolder {

        private TextView nameView;
        private TextView descView;

        public RepoHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.repo_item_name);
            descView = itemView.findViewById(R.id.repo_item_desc);
        }

        private void bind(Repo repo) {
            nameView.setText(repo.getName());
            descView.setText(repo.getDescription());
        }
    }

    private class RepoAdapter extends RecyclerView.Adapter<RepoHolder> {

        @NonNull
        @Override
        public RepoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.repo_item, parent, false);
            return new RepoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RepoHolder holder, int position) {
            holder.bind(repos.get(position));
        }

        @Override
        public int getItemCount() {
            return repos.size();
        }
    }

    public static Fragment newInstance(CompanyItem companyItem) {
        RepoFragment repoFragment = new RepoFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(COMPANY_ITEM_BUNDLE, companyItem);
        repoFragment.setArguments(bundle);
        return repoFragment;
    }
}
