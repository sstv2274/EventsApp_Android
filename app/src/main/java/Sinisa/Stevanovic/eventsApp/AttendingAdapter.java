package Sinisa.Stevanovic.eventsApp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class AttendingAdapter extends BaseAdapter {
    private Context context;
    private List<Object> items;

    public AttendingAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() { return items.size(); }

    @Override
    public Object getItem(int position) { return items.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public int getViewTypeCount() { return 2; }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return 0; // zaglavlje
        } else {
            return 1; // Dogadjaj
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == 1;
    }

    //ViewHolder
    private static class HeaderViewHolder {
        TextView tvHeaderTitle;
    }

    //Viewholder za dogadjaj
    private static class EventViewHolder {
        TextView tvName;
        TextView tvDateTime;
        TextView tvLocation;
        Button btnRate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);

        if (type == 0) {
            HeaderViewHolder headerHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
                headerHolder = new HeaderViewHolder();
                headerHolder.tvHeaderTitle = convertView.findViewById(R.id.tvHeaderTitle);
                convertView.setTag(headerHolder);
            } else {
                headerHolder = (HeaderViewHolder) convertView.getTag();
            }

            headerHolder.tvHeaderTitle.setText((String) items.get(position));

        } else {
            EventViewHolder eventHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_attending_event, parent, false);
                eventHolder = new EventViewHolder();
                eventHolder.tvName = convertView.findViewById(R.id.tvAttendingName);
                eventHolder.tvDateTime = convertView.findViewById(R.id.tvAttendingDateTime);
                eventHolder.tvLocation = convertView.findViewById(R.id.tvAttendingLocation);
                eventHolder.btnRate = convertView.findViewById(R.id.btnRateEvent);
                convertView.setTag(eventHolder);
            } else {
                eventHolder = (EventViewHolder) convertView.getTag();
            }

            Event event = (Event) items.get(position);

            eventHolder.tvName.setText(event.getName());
            eventHolder.tvDateTime.setText(event.getDateTime());
            eventHolder.tvLocation.setText(event.getLocation());

            if (event.isPast()) {
                eventHolder.btnRate.setVisibility(View.VISIBLE);
                eventHolder.btnRate.setOnClickListener(v -> {
                    Intent intent = new Intent(context, RatingActivity.class);
                    intent.putExtra("EVENT_NAME", event.getName());
                    context.startActivity(intent);
                });
            } else {
                eventHolder.btnRate.setVisibility(View.GONE);
            }
        }
        return convertView;
    }
}