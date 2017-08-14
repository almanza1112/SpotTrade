package almanza1112.spottrade;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by almanza1112 on 8/11/17.
 */

class YourSpotsAdapter extends RecyclerView.Adapter<YourSpotsAdapter.RecyclerViewHolder>{

    private Activity activity;
    YourSpotsAdapter(Activity activity){
        this.activity = activity;
    }

    @Override
    public YourSpotsAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{

        RecyclerViewHolder(View view){
            super(view);
        }
    }
}

