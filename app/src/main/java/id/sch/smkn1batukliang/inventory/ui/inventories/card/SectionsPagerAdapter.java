package id.sch.smkn1batukliang.inventory.ui.inventories.card;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import id.sch.smkn1batukliang.inventory.ui.inventories.card.goods.ListGoodsFragment;
import id.sch.smkn1batukliang.inventory.ui.inventories.card.room.ListRoomFragment;

public class SectionsPagerAdapter extends FragmentStateAdapter {
    private Fragment fragment = new Fragment();

    public SectionsPagerAdapter(FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                fragment = new ListGoodsFragment();
                break;
            case 1:
                fragment = new ListRoomFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
