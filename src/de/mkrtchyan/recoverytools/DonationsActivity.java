package de.mkrtchyan.recoverytools;

/*
 * Copyright (c) 2013 Ashot Mkrtchyan
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;

import donations.DonationsFragment;


public class DonationsActivity extends FragmentActivity {

    /**
     * Google
     */
    private static final boolean GOOGLE_PLAY = true;
    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhHa/9/sYU2dbF6nQqGzNktvxb+83Ed/inkK8cbiEkcRjw/t/Okge6UghlyYEXcZLJL9TDPAlraktUZZ/XH8+ZpgdNlO+UeQTD4Yl9ReZ/ujQ151g/RLrVNi7NF4SQ1jD20RmX2lCUhbl5cPi6UKL/bHFeZwjE0pOr48svW0nXbRfpgSSk3V/DaV1igTX66DuFUITKi0gQGD8XAVsrOcQRQtr4wHfdgyMQR9m0vPPzpFoDD8SZZFCp9UgvuzqdwYqY8kr7ZcyxuQhaNlcx74hpFQ9MJteRTII+ii/pHfWDh0hDMqcodm4UD9rISmPSvlLR3amfSg4Vm6ObWFiVe4qVwIDAQAB";
    private static final String[] GOOGLE_CATALOG = new String[]{"donate_0_50", "donate_1", "donate_2", "donate_3", "donate_5"};

    /**
     * PayPal
     */
    private static final boolean PAYPAL = true;
    private static final String PAYPAL_USER = "ashotmkrtchyan1995@gmail.com";
    private static final String PAYPAL_CURRENCY_CODE = "EUR";
    private static final String PAYPAL_ITEM_NAME = "Recovery-Tools donation";

    /**
     * Flattr
     */
    private static final boolean FLATTR = true;
    private static final String FLATTR_PROJECT_URL = "http://github.com/ashotmkrtchyan1995/Recovery-Tools";
    private static final String FLATTR_URL = "flattr.com/thing/1853888/ashotmkrtchyan1995Recovery-Tools-on-GitHub";

    LinearLayout Layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Layout = new LinearLayout(this);
        Layout.setHorizontalGravity(LinearLayout.HORIZONTAL);
        Layout.setId(1111111);

        setContentView(Layout);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DonationsFragment donationsFragment;
        donationsFragment = DonationsFragment.newInstance(BuildConfig.DEBUG, GOOGLE_PLAY, GOOGLE_PUBKEY, GOOGLE_CATALOG,
                getResources().getStringArray(R.array.donation_google_catalog_values), PAYPAL, PAYPAL_USER, PAYPAL_CURRENCY_CODE, PAYPAL_ITEM_NAME, FLATTR, FLATTR_PROJECT_URL, FLATTR_URL);

        ft.replace(Layout.getId(), donationsFragment, "donationsFragment");
        ft.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}