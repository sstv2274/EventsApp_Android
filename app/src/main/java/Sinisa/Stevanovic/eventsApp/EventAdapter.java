package Sinisa.Stevanovic.eventsApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends BaseAdapter {
    private Context context;
    private List<Event> events;

    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = new ArrayList<>(events);
    }

    // Zahtev iz zadatka: implementirati metodu za postavljanje nove liste
    public void setEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged(); // Osvežava ListView
    }

    // Zahtev iz zadatka: implementirati metodu za brisanje svih elemenata
    public void clearEvents() {
        this.events.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return events.size(); }

    @Override
    public Object getItem(int position) { return events.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    // Zahtev iz zadatka: ViewHolder šablon
    private static class ViewHolder {
        LinearLayout llContainer;
        ImageView ivEventImage;
        TextView tvEventName;
        TextView tvEventCategory;
        TextView tvEventLocationDate;
        TextView tvFeaturedBadge;
        TextView tvFreeSpots;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Ako View još nije kreiran, inflatujemo ga i povezujemo elemente (ViewHolder)
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
            holder = new ViewHolder();
            holder.llContainer = convertView.findViewById(R.id.llEventItemContainer);
            holder.ivEventImage = convertView.findViewById(R.id.ivEventImage);
            holder.tvEventName = convertView.findViewById(R.id.tvEventName);
            holder.tvEventCategory = convertView.findViewById(R.id.tvEventCategory);
            holder.tvEventLocationDate = convertView.findViewById(R.id.tvEventLocationDate);
            holder.tvFeaturedBadge = convertView.findViewById(R.id.tvFeaturedBadge);
            holder.tvFreeSpots = convertView.findViewById(R.id.tvFreeSpots);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag(); // Recikliramo postojeći View
        }

        Event event = events.get(position);

        // Postavljanje podataka
        holder.ivEventImage.setImageResource(event.getImageResId());
        holder.tvEventName.setText(event.getName());
        holder.tvEventCategory.setText(event.getCategory());
        holder.tvEventLocationDate.setText(event.getDateTime() + "\n" + event.getLocation());

        // Vizuelna distinkcija za PROMOTED dogadjaje
        if (event.isPromoted()) {
            holder.llContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.promoted_background));
            holder.tvFeaturedBadge.setVisibility(View.VISIBLE);
            holder.tvFreeSpots.setVisibility(View.VISIBLE);

            // Računanje slobodnih mesta
            int slobodnaMesta = event.getCapacity() - event.getAttendingCount();
            String freeSpotsText = context.getString(R.string.free_spots_format, slobodnaMesta, event.getCapacity());
            holder.tvFreeSpots.setText(freeSpotsText);
        } else {
            holder.llContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.tvFeaturedBadge.setVisibility(View.GONE);
            holder.tvFreeSpots.setVisibility(View.GONE);
        }

        return convertView;
    }
}