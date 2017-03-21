package deputy.android.com.deputyliang;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import deputy.android.com.deputyliang.adapter.ShiftAdapter;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.testing.ShiftTestUtil;

public class MainActivity extends AppCompatActivity implements ShiftAdapter.ShiftAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private ShiftAdapter mShiftAdapter;
    private TextView mEmptyMessageDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_shifts);
        mEmptyMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mShiftAdapter = new ShiftAdapter(this);

        mRecyclerView.setAdapter(mShiftAdapter);

        mShiftAdapter.setShiftData(ShiftTestUtil.generateArrayOfFakeShift());
    }

    @Override
    public void onClick(Shift shift) {
        Toast.makeText(this, String.valueOf(shift.getShift_id()), Toast.LENGTH_SHORT).show();
    }

    private void showShiftDataView() {
        mEmptyMessageDisplay.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.GONE);
        /* Then, show the error */
        mEmptyMessageDisplay.setVisibility(View.VISIBLE);
    }
}
