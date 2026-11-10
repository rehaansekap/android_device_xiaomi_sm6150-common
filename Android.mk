#
# Copyright (C) 2026 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

LOCAL_PATH := $(call my-dir)

ifneq ($(filter sweet2,$(TARGET_DEVICE)),)

include $(CLEAR_VARS)

# WiFi firmware symlinks
WCNSS_INI_SYMLINK := $(TARGET_OUT_VENDOR)/firmware/wlan/qca_cld/WCNSS_qcom_cfg.ini
$(WCNSS_INI_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "WCNSS config ini link: $@"
	@mkdir -p $(dir $@)
	@rm -rf $@
	$(hide) ln -sf /vendor/etc/wifi/$(notdir $@) $@

WCNSS_MAC_SYMLINK := $(TARGET_OUT_VENDOR)/firmware/wlan/qca_cld/wlan_mac.bin
$(WCNSS_MAC_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "WCNSS MAC bin link: $@"
	@mkdir -p $(dir $@)
	@rm -rf $@
	$(hide) ln -sf /mnt/vendor/persist/$(notdir $@) $@

ALL_DEFAULT_INSTALLED_MODULES += $(WCNSS_INI_SYMLINK) $(WCNSS_MAC_SYMLINK)

# Adreno EGL symlinks
EGL_LIBS := libEGL_adreno.so libGLESv2_adreno.so libq3dtools_adreno.so
EGL_32_SYMLINKS := $(addprefix $(TARGET_OUT_VENDOR)/lib/,$(notdir $(EGL_LIBS)))
$(EGL_32_SYMLINKS): $(LOCAL_INSTALLED_MODULE)
	@echo "EGL 32 lib link: $@"
	@mkdir -p $(dir $@)
	@rm -rf $@
	$(hide) ln -sf egl/$(notdir $@) $@

EGL_64_SYMLINKS := $(addprefix $(TARGET_OUT_VENDOR)/lib64/,$(notdir $(EGL_LIBS)))
$(EGL_64_SYMLINKS): $(LOCAL_INSTALLED_MODULE)
	@echo "EGL 64 lib link: $@"
	@mkdir -p $(dir $@)
	@rm -rf $@
	$(hide) ln -sf egl/$(notdir $@) $@

ALL_DEFAULT_INSTALLED_MODULES += $(EGL_32_SYMLINKS) $(EGL_64_SYMLINKS)

# IMS symlinks
IMS_LIBS := libimscamera_jni.so libimsmedia_jni.so
IMS_SYMLINKS := $(addprefix $(TARGET_OUT_SYSTEM_EXT_APPS_PRIVILEGED)/ims/lib/arm64/,$(notdir $(IMS_LIBS)))
$(IMS_SYMLINKS): $(LOCAL_INSTALLED_MODULE)
	@echo "IMS lib link: $@"
	@mkdir -p $(dir $@)
	@rm -rf $@
	$(hide) ln -sf /system_ext/lib64/$(notdir $@) $@

ALL_DEFAULT_INSTALLED_MODULES += $(IMS_SYMLINKS)

# WFD symlink
WFD_LIBS := libwfdnative.so
WFD_SYMLINKS := $(addprefix $(TARGET_OUT_SYSTEM_EXT_APPS_PRIVILEGED)/WfdService/lib/arm64/,$(notdir $(WFD_LIBS)))
$(WFD_SYMLINKS): $(LOCAL_INSTALLED_MODULE)
	@echo "WFD lib link: $@"
	@mkdir -p $(dir $@)
	@rm -rf $@
	$(hide) ln -sf /system_ext/lib64/$(notdir $@) $@

ALL_DEFAULT_INSTALLED_MODULES += $(WFD_SYMLINKS)

# Firmware mountpoints
FIRMWARE_MOUNTPOINTS := $(TARGET_OUT_VENDOR)/firmware_mnt $(TARGET_OUT_VENDOR)/bt_firmware $(TARGET_OUT_VENDOR)/dsp
$(FIRMWARE_MOUNTPOINTS): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating firmware mount point: $@"
	@mkdir -p $@

ALL_DEFAULT_INSTALLED_MODULES += $(FIRMWARE_MOUNTPOINTS)

# RFS symlinks
RFS_SYMLINK_DIRS := \
    apq/gnss \
    mdm/adsp mdm/cdsp mdm/mpss mdm/ois mdm/slpi mdm/tn mdm/wpss \
    msm/adsp msm/cdsp msm/mpss msm/ois msm/slpi msm/wpss

define add-rfs-symlinks
RFS_$(1)_SYMLINKS := \
    $(TARGET_OUT_VENDOR)/rfs/$(1)/readwrite \
    $(TARGET_OUT_VENDOR)/rfs/$(1)/shared \
    $(TARGET_OUT_VENDOR)/rfs/$(1)/hlos \
    $(TARGET_OUT_VENDOR)/rfs/$(1)/ramdumps \
    $(TARGET_OUT_VENDOR)/rfs/$(1)/readonly/firmware \
    $(TARGET_OUT_VENDOR)/rfs/$(1)/readonly/vendor/firmware

$(TARGET_OUT_VENDOR)/rfs/$(1)/readwrite: $(LOCAL_INSTALLED_MODULE)
	@mkdir -p $$(dir $$@)
	@rm -rf $$@
	$$(hide) ln -sf /mnt/vendor/persist/rfs/$(1) $$@

$(TARGET_OUT_VENDOR)/rfs/$(1)/shared: $(LOCAL_INSTALLED_MODULE)
	@mkdir -p $$(dir $$@)
	@rm -rf $$@
	$$(hide) ln -sf /mnt/vendor/persist/rfs/shared $$@

$(TARGET_OUT_VENDOR)/rfs/$(1)/hlos: $(LOCAL_INSTALLED_MODULE)
	@mkdir -p $$(dir $$@)
	@rm -rf $$@
	$$(hide) ln -sf /mnt/vendor/persist/hlos_rfs/shared $$@

$(TARGET_OUT_VENDOR)/rfs/$(1)/ramdumps: $(LOCAL_INSTALLED_MODULE)
	@mkdir -p $$(dir $$@)
	@rm -rf $$@
	$$(hide) ln -sf /data/vendor/tombstones/rfs/$(2) $$@

$(TARGET_OUT_VENDOR)/rfs/$(1)/readonly/firmware: $(LOCAL_INSTALLED_MODULE)
	@mkdir -p $$(dir $$@)
	@rm -rf $$@
	$$(hide) ln -sf /vendor/firmware_mnt $$@

$(TARGET_OUT_VENDOR)/rfs/$(1)/readonly/vendor/firmware: $(LOCAL_INSTALLED_MODULE)
	@mkdir -p $$(dir $$@)
	@rm -rf $$@
	$$(hide) ln -sf /vendor/firmware $$@

ALL_DEFAULT_INSTALLED_MODULES += $$(RFS_$(1)_SYMLINKS)
endef

$(eval $(call add-rfs-symlinks,apq/gnss,modem))
$(eval $(call add-rfs-symlinks,mdm/adsp,lpass))
$(eval $(call add-rfs-symlinks,mdm/cdsp,cdsp))
$(eval $(call add-rfs-symlinks,mdm/mpss,modem))
$(eval $(call add-rfs-symlinks,mdm/ois,ois))
$(eval $(call add-rfs-symlinks,mdm/slpi,slpi))
$(eval $(call add-rfs-symlinks,mdm/tn,tn))
$(eval $(call add-rfs-symlinks,mdm/wpss,wpss))
$(eval $(call add-rfs-symlinks,msm/adsp,lpass))
$(eval $(call add-rfs-symlinks,msm/cdsp,cdsp))
$(eval $(call add-rfs-symlinks,msm/mpss,modem))
$(eval $(call add-rfs-symlinks,msm/ois,ois))
$(eval $(call add-rfs-symlinks,msm/slpi,slpi))
$(eval $(call add-rfs-symlinks,msm/wpss,wpss))

endif
