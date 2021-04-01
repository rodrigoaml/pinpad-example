package com.me.pagar.mpos.example;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.databinding.ObservableField;
import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroid.MposListener;
import me.pagar.mposandroid.MposPaymentResult;
import me.pagar.mposandroid.PaymentMethod;

public class PagarMeSDKInterface {

    private String LOG_TAG_API_INTERFACE = "PINPADExample PAGARME";
    private HTTPComm transaction, refund;
    private Context context;
    private String encryptionKey;
    private String apiKey;

    private String TRANSACTION_URL = "https://api.pagar.me/1/transactions";
    private String currentTransaction = null;
    public TransactionViewModel transactionViewModel;

    private void configureEMVApplications(Mpos mpos, int amount, String paymentMethod) {
        int method;
        if (paymentMethod.compareTo("Credit") == 0) {
            method = PaymentMethod.CreditCard;
        } else {
            method = PaymentMethod.DebitCard;
        }
        mpos.payAmount(amount, null, method);
        transactionViewModel.step.set("Insira ou passe o cartão");
    }

    public PagarMeSDKInterface(String apiKey, String encryptionKey) {
        this.apiKey = apiKey;
        this.encryptionKey = encryptionKey;
        this.transactionViewModel = new TransactionViewModel();
    }

    public void init(BluetoothDevice device, final Context context, final int amount, final String paymentMethod, final Boolean updateTable) throws IOException {
        final Mpos mpos = new Mpos(device, this.encryptionKey, context);
        this.context = context;
        transaction = new HTTPComm();


        transaction.addListener(new HTTPEvent() {
            @Override
            public void onResponse(JSONObject response, Mpos mpos) {
                Log.d(LOG_TAG_API_INTERFACE, response.toString());
                try {
                    String cardEmvResponse = null;
                    int acquirerResponseCode = 0;
                    if (response.has("card_emv_response")) {
                        cardEmvResponse = (String) response.get("card_emv_response");
                    }
                    if (response.has("acquirer_response_code")) {
                        acquirerResponseCode = Integer.parseInt((String) response.get("acquirer_response_code"));
                    }
                    transactionViewModel.step.set("Transaction feita com sucesso");
                    mpos.finishTransaction(true, acquirerResponseCode, cardEmvResponse);
                } catch (JSONException e) {
                    Log.e(LOG_TAG_API_INTERFACE, e.toString());
                }
            }

            @Override
            public void onErrorResponse(int statusCode, String message) {
                transactionViewModel.step.set("Erro ao efetuar transaction");
                Log.d(LOG_TAG_API_INTERFACE, "Status code: " + statusCode);
                Log.d(LOG_TAG_API_INTERFACE, "Message: " + message);
                Toast.makeText(context, "[" + statusCode + "]" + ": " + message, Toast.LENGTH_SHORT).show();
                mpos.close("Pagar.me server error");
            }
        });
        mpos.addListener(new MposListener() {
            @Override
            public void bluetoothConnected() {
                Log.d(LOG_TAG_API_INTERFACE, "Bluetooth connected.");
                transactionViewModel.step.set("Bluetooth conectado");
                mpos.initialize();

            }

            @Override
            public void bluetoothDisconnected() {
                Log.d(LOG_TAG_API_INTERFACE, "Bluetooth disconnected.");
            }

            @Override
            public void bluetoothErrored(int i) {
                Log.d(LOG_TAG_API_INTERFACE, "Received bluetooth error " + i);
            }

            @Override
            public void receiveInitialization() {
                Log.d(LOG_TAG_API_INTERFACE, "receive initialization!");
                transactionViewModel.step.set("Transaction iniciada");
                try {
                    if (updateTable) {
                        transactionViewModel.step.set("Iniciando update de tabelas");
                        mpos.downloadEMVTablesToDevice(true);
                    } else {
                        configureEMVApplications(mpos, amount, paymentMethod);
                    }

                } catch (Exception e) {
                    Log.e(LOG_TAG_API_INTERFACE, "Got error in initialization and table update " + e.getMessage());
                }

            }

            @Override
            public void receiveClose() {
                Log.d(LOG_TAG_API_INTERFACE, "receiveClose");
                mpos.closeConnection();
            }

            @Override
            public void receiveNotification(String s) {
                Log.d(LOG_TAG_API_INTERFACE, s);
                transactionViewModel.step.set(s);
            }

            @Override
            public void receiveOperationCompleted() {

            }

            @Override
            public void receiveOperationCancelled() {

            }

            @Override
            public void receiveCardHash(String cardHash, MposPaymentResult result) {
                transactionViewModel.step.set("Card hash gerado");

                currentTransaction = result.localTransactionId;

                Map<String, Object> params = new HashMap<String, Object>();

                ArrayList<Object> splitRules = new ArrayList<Object>();
                Map<String, Object> splitRuleA = new HashMap<String, Object>();
                splitRuleA.put("amount", 150);
                splitRuleA.put("charge_processing_fee", true);
                splitRuleA.put("liable", true);
                splitRuleA.put("recipient_id", "ADD_RECIPIENT_ID_AQUI");

                Map<String, Object> splitRuleB = new HashMap<String, Object>();
                splitRuleB.put("amount", 850);
                splitRuleB.put("charge_processing_fee", false);
                splitRuleB.put("liable", false);
                splitRuleB.put("recipient_id", "ADD_RECIPIENT_ID_AQUI");

                splitRules.add(splitRuleA);
                splitRules.add(splitRuleB);

                params.put("api_key", apiKey);
                params.put("amount", amount);
                params.put("card_hash", cardHash);
                params.put("installments", 1);
                params.put("local_time", new Date().getTime());
                params.put("split_rules", splitRules);

                transaction.request(context, TRANSACTION_URL, params, HTTPComm.HTTPMethod.POST, mpos);
            }

            @Override
            public void receiveTableUpdated(boolean b) {
                Log.d(LOG_TAG_API_INTERFACE, "received table updated loaded = " + b);
                transactionViewModel.step.set("Tabelas EMV atualizadas com sucesso");
                configureEMVApplications(mpos, amount, paymentMethod);
            }

            @Override
            public void receiveFinishTransaction() {
                Log.d(LOG_TAG_API_INTERFACE, "Finished transaction");
                transactionViewModel.step.set("Transaction feita com sucesso");
                mpos.close("TRANSACTION APPROVED");
            }

            @Override
            public void receiveError(int i) {
                Log.d(LOG_TAG_API_INTERFACE, "Received error " + i);

                String errMsg = "ERROR: " + i + ". TRY AGAIN";
                mpos.close(errMsg);
            }
        });
        mpos.openConnection(false);
    }


    public static class TransactionViewModel {
        public ObservableField<String> step = new ObservableField<>();
        public TransactionViewModel () {}
    }
}
