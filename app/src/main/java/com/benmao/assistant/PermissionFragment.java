package com.benmao.assistant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.benmao.assistant.databinding.FragmentPermissionBinding;

public class PermissionFragment extends Fragment {

    private FragmentPermissionBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPermissionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view);

        binding.cardAccessibility.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "请手动打开无障碍设置", Toast.LENGTH_SHORT).show();
            }
        });

        binding.cardOverlay.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "请手动打开悬浮窗权限", Toast.LENGTH_SHORT).show();
            }
        });

        updateStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        if (binding == null) return;
        boolean a11y = getActivity() instanceof MainActivity &&
                ((MainActivity) getActivity()).isAccessibilityEnabled();
        boolean overlay = getActivity() != null &&
                Settings.canDrawOverlays(getActivity());
        binding.tvAccessibilityStatus.setText(a11y ? "已开启" : "未开启");
        binding.tvOverlayStatus.setText(overlay ? "已开启" : "未开启");
    }
}
