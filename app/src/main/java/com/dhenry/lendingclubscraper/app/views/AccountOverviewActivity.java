package com.dhenry.lendingclubscraper.app.views;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dhenry.lendingclubscraper.app.R;
import com.dhenry.lendingclubscraper.app.constants.LendingClubConstants;
import com.dhenry.lendingclubscraper.app.lendingClub.LendingClubAPI;
import com.dhenry.lendingclubscraper.app.lendingClub.ResponseHandler;
import com.dhenry.lendingclubscraper.app.lendingClub.impl.LendingClubAPIClient;
import com.dhenry.lendingclubscraper.app.persistence.models.AccountSummaryData;
import com.dhenry.lendingclubscraper.app.persistence.models.NARCalculationData;
import com.dhenry.lendingclubscraper.app.persistence.models.UserData;
import com.dhenry.lendingclubscraper.app.utilities.NumberFormats;
import com.dhenry.lendingclubscraper.app.views.adapters.KeyValueAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Author: Dave
 */
public class AccountOverviewActivity extends ListActivity {

    private KeyValueAdapter adapter;

    private Button accountDetailsButton;
    private Button browseNotesButton;
    private NARCalculationData narCalculationData;
    private AccountSummaryData accountSummaryData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_overview);

        accountDetailsButton = (Button)findViewById(R.id.account_details_button);
        browseNotesButton = (Button)findViewById(R.id.browse_notes_button);

        adapter = new KeyValueAdapter(this);
        setListAdapter(adapter);

        final UserData currentUser = getIntent().getParcelableExtra(LendingClubConstants.CURRENT_USER);

        LendingClubAPI lendingClubAPI = new LendingClubAPIClient(this);

        if (savedInstanceState != null) {
            accountSummaryData = savedInstanceState.getParcelable(AccountSummaryData.class.getName());
            narCalculationData = savedInstanceState.getParcelable(NARCalculationData.class.getName());
        }

        if (accountSummaryData != null) {
            addAccountSummaryDataToAdapter(accountSummaryData);
        } else {
            lendingClubAPI.getAccountSummary(currentUser, new AccountSummaryResponseHandler());
        }

        if (narCalculationData != null) {
            addNARCalculationDataToAdapter(narCalculationData);
        } else {
            lendingClubAPI.getNetAnnualizedReturnData(currentUser, new NetAnnualizedReturnHandler());
        }

        accountDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountDetailsIntent = new Intent(AccountOverviewActivity.this, AccountDetailsActivity.class);
                accountDetailsIntent.putExtra(LendingClubConstants.CURRENT_USER, currentUser);
                startActivity(accountDetailsIntent);
            }
        });

        browseNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browseNotesIntent = new Intent(AccountOverviewActivity.this, BrowseNotesActivity.class);
                browseNotesIntent.putExtra(LendingClubConstants.CURRENT_USER, currentUser);
                startActivity(browseNotesIntent);
            }
        });
    }

    private class NetAnnualizedReturnHandler implements ResponseHandler<NARCalculationData> {

        @Override
        public void onTaskError(Exception exception) {
            Toast.makeText(AccountOverviewActivity.this,"Net annualized return retrieval failed: "
                    + exception.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onTaskSuccess(NARCalculationData result) {
            narCalculationData = result;
            addNARCalculationDataToAdapter(result);
        }
    }

    private void addNARCalculationDataToAdapter(NARCalculationData narCalculationData) {
        NumberFormat percentFormat = NumberFormats.PERCENT_FORMAT;

        adapter.add(new Pair<String, String>("Adjusted Net Annualized Return",
                percentFormat.format(narCalculationData.getAdjustedNetAnnualizedReturn())));
        adapter.add(new Pair<String, String>("Weighted Average Rate",
                percentFormat.format(narCalculationData.getWeightedAverageRate())));
    }

    private class AccountSummaryResponseHandler implements ResponseHandler<AccountSummaryData> {

        @Override
        public void onTaskError(Exception exception) {
            Toast.makeText(AccountOverviewActivity.this,"Account summary retrieval failed: "
                    + exception.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onTaskSuccess(AccountSummaryData result) {
            accountSummaryData = result;
            addAccountSummaryDataToAdapter(result);
        }
    }

    private void addAccountSummaryDataToAdapter(AccountSummaryData accountSummaryData) {
        NumberFormat currencyFormat = NumberFormats.CURRENCY_FORMAT;

        adapter.add(new Pair<String, String>("Total Payments",
                currencyFormat.format(accountSummaryData.getTotalPayments())));
        adapter.add(new Pair<String, String>("Account Value",
                currencyFormat.format(accountSummaryData.getAccountValue())));
        adapter.add(new Pair<String, String>("Outstanding Principle",
                currencyFormat.format(accountSummaryData.getOutstandingPrinciple())));
        adapter.add(new Pair<String, String>("Available Cash",
                currencyFormat.format(accountSummaryData.getAvailableCash())));
        adapter.add(new Pair<String, String>("In Funding Notes",
                currencyFormat.format(accountSummaryData.getInFundingNotes())));
        adapter.add(new Pair<String, String>("Adjusted Account Value",
                currencyFormat.format(accountSummaryData.getAdjustedAccountValues())));
        adapter.add(new Pair<String, String>("Interest Received",
                currencyFormat.format(accountSummaryData.getInterestReceived())));
        adapter.add(new Pair<String, String>("Adjustment for Past-Due Notes",
                currencyFormat.format(accountSummaryData.getPastDueNotesAdjustment())));
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        View empty = findViewById(R.id.empty);
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setEmptyView(empty);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (this.accountSummaryData != null) {
            savedInstanceState.putParcelable(AccountSummaryData.class.getName(), accountSummaryData);
        }

        if (this.narCalculationData != null) {
            savedInstanceState.putParcelable(NARCalculationData.class.getName(), narCalculationData);
        }
    }
}
