package com.andry.gitcompanysearch.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.andry.gitcompanysearch.fragments.MainFragment;
import com.andry.gitcompanysearch.model.CompanyItem;

public class MainActivity extends SingleFragmentActivity implements MainFragment.CompanySelectListener {

    @Override
    public Fragment createFragment() {
        return MainFragment.newInstance();
    }

    @Override
    public void onCompanySelected(CompanyItem companyItem) {
        Intent intent = RepoActivity.newIntent(this, companyItem);
        startActivity(intent);
    }
}
