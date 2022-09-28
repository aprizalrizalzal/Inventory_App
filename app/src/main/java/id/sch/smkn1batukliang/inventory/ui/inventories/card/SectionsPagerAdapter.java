package id.sch.smkn1batukliang.inventory.ui.inventories.card;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import id.sch.smkn1batukliang.inventory.ui.inventories.card.room.ListRoomFragment;
import id.sch.smkn1batukliang.inventory.ui.inventories.card.goods.ListGoodsFragment;

public class SectionsPagerAdapter extends FragmentStateAdapter {

    public SectionsPagerAdapter(AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ListGoodsFragment();
                break;
            case 1:
                fragment = new ListRoomFragment();
                break;
        }
        assert fragment != null;
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
