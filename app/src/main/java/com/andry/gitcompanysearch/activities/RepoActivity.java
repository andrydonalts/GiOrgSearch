package com.andry.gitcompanysearch.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.andry.gitcompanysearch.fragments.RepoFragment;
import com.andry.gitcompanysearch.model.CompanyItem;

public class RepoActivity extends SingleFragmentActivity {
    private static final String COMPANY_NAME_EXTRA = "COMPANY_ITEM_EXTRA";

    private CompanyItem companyItem;

    @Override
    public Fragment createFragment() {
        companyItem = getIntent().getParcelableExtra(COMPANY_NAME_EXTRA);
        return RepoFragment.newInstance(companyItem);
    }

    public static Intent newIntent(Context context, CompanyItem item) {
        Intent intent = new Intent(context, RepoActivity.class);
        intent.putExtra(COMPANY_NAME_EXTRA, item);
        return intent;
    }
}
