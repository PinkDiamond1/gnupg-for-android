/*
 * Copyright (C) 2010 Thialfihar <thi@thialfihar.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.guardianproject.gpg;

import info.guardianproject.gpg.apg_compat.Apg;

import java.math.BigInteger;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.freiheit.gnupg.GnuPGContext;
import com.freiheit.gnupg.GnuPGKey;

public class KeyListAdapter extends BaseAdapter {
    protected LayoutInflater mInflater;
    protected ListView mParent;
    protected String mSearchString;
    protected Activity mActivity;
    protected long mSelectedKeyIds[];

    private GnuPGContext mCtx = null;
    private GnuPGKey[] mKeyArray;

    public KeyListAdapter(Activity activity, ListView parent, String action,
                                      String searchString, long selectedKeyIds[]) {
        mActivity = activity;
        mParent = parent;
        mSearchString = searchString;
        mSelectedKeyIds = selectedKeyIds;

        mInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mCtx = new GnuPGContext();
        // set the homeDir option to our custom home location
        mCtx.setEngineInfo(mCtx.getProtocol(), mCtx.getFilename(),
                NativeHelper.app_home.getAbsolutePath());
        if (action == null || !action.equals(Apg.Intent.SELECT_SECRET_KEY))
            mKeyArray = mCtx.listKeys();
        else
            mKeyArray = mCtx.listSecretKeys();
        if (mKeyArray == null) {
            Log.e(GPGApplication.TAG, "keyArray is null");
        }
    }

    @Override
    public boolean isEnabled(int position) {
        GnuPGKey key = mKeyArray[position];
        return (!key.isDisabled()
                && !key.isExpired()
                && !key.isRevoked()
                && !key.isInvalid());
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public int getCount() {
        return mKeyArray.length;
    }

    public Object getItem(int position) {
        GnuPGKey key = mKeyArray[position];
        String[] ret = new String[3];
        ret[0] = key.getName();
        ret[1] = key.getEmail();
        ret[2] = key.getComment();
        return ret;
    }

    public long getItemId(int position) {
        String keyId = mKeyArray[position].getKeyID();
        return new BigInteger(keyId, 16).longValue(); // MASTER_KEY_ID
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        GnuPGKey key = mKeyArray[position];
        View view = mInflater.inflate(R.layout.key_list_item, null);
        boolean usable = isEnabled(position);

        TextView mainUserId = (TextView) view.findViewById(R.id.mainUserId);
        TextView mainUserIdRest = (TextView) view.findViewById(R.id.mainUserIdRest);
        TextView keyId = (TextView) view.findViewById(R.id.keyId);
        TextView status = (TextView) view.findViewById(R.id.status);

        mainUserId.setText(key.getName());
        mainUserIdRest.setText(key.getEmail());
        keyId.setText(key.getKeyID());
        status.setText(R.string.unknownStatus);

        if (mainUserIdRest.getText().length() == 0) {
            mainUserIdRest.setVisibility(View.GONE);
        }

        if (usable)
            status.setText(R.string.usable);
        else if (key.isDisabled())
            status.setText(R.string.disabled);
        else if (key.isExpired())
            status.setText(R.string.expired);
        else if (key.isInvalid())
            status.setText(R.string.invalid);
        else if (key.isRevoked())
            status.setText(R.string.revoked);
        else
            status.setText(R.string.noKey);

        status.setText(status.getText() + " ");

        if (!usable) {
            mParent.setItemChecked(position, false);
        }

        view.setEnabled(usable);
        mainUserId.setEnabled(usable);
        mainUserIdRest.setEnabled(usable);
        keyId.setEnabled(usable);
        status.setEnabled(usable);

        return view;
    }
}