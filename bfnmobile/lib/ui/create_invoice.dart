import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:bfnlibrary/util/prefs.dart';
import 'package:bfnlibrary/util/slide_right.dart';
import 'package:bfnlibrary/util/snack.dart';
import 'package:bfnmobile/ui/buy_offer.dart';
import 'package:bfnmobile/ui/network_accounts.dart';
import 'package:flutter/material.dart';

import '../bloc.dart';

class CreateInvoice extends StatefulWidget {
  @override
  _CreateInvoiceState createState() => _CreateInvoiceState();
}

class _CreateInvoiceState extends State<CreateInvoice>
    implements SnackBarListener {
  var _key = GlobalKey<ScaffoldState>();
  var _formKey = GlobalKey<FormState>();
  var _amountKey = GlobalKey<FormState>();
  var _vatKey = GlobalKey<FormState>();
  var _invoiceKey = GlobalKey<FormState>();
  var _descKey = GlobalKey<FormState>();
  AccountInfo account, tradingAccount;
  String amount, vat, totalAmount, invoiceNumber, description;

  @override
  void initState() {
    super.initState();
    _init();
  }

  _init() async {
    account = await Prefs.getAccount();
    setState(() {});
  }

  _setTotalAmount() {
    if (amount == null || amount.isEmpty) {
      return;
    }
    if (vat == null || vat.isEmpty) {
      return;
    }
    var amt = double.parse(amount);
    var tax = double.parse(vat);
    var tot = amt + (amt * (tax / 100));
    setState(() {
      totalAmount = getCurrency(tot, context);
    });
  }

  String getCurrency(double amt, BuildContext context) {
    return getFormattedAmount(amt.toString(), context);
  }

  String message;
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        title: Text('Create Invoice'),
        bottom: PreferredSize(
          preferredSize: Size.fromHeight(120),
          child: Column(
            children: <Widget>[
              account == null
                  ? Container()
                  : NameBadge(
                      account: account,
                      nodeStyle: Styles.whiteSmall,
                      nameStyle: Styles.blackBoldMedium,
                      elevation: 2,
                    ),
              SizedBox(
                height: 20,
              ),
              StreamBuilder<String>(
                  stream: bfnBloc.fcmStream,
                  initialData: 'No network message yet',
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      debugPrint(
                          '😡  😡  😡  😡  CreateInvoice: FCM message arrived on Stream: ${snapshot.data}  😡  😡  😡  😡 ');
                      message = snapshot.data;
                    }
                    return Text(
                      '$message',
                      style: Styles.whiteSmall,
                    );
                  }),
              SizedBox(
                height: 20,
              ),
            ],
          ),
        ),
      ),
      backgroundColor: Colors.brown[50],
      body: ListView(
        children: <Widget>[
          SizedBox(
            height: 12,
          ),
          Card(
            child: Column(
              children: <Widget>[
                SizedBox(
                  height: 12,
                ),
                RaisedButton(
                  elevation: 2,
                  child: Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Text(
                      'Select Supplier',
                      style: Styles.whiteSmall,
                    ),
                  ),
                  onPressed: _onAccountRequested,
                ),
                SizedBox(
                  height: 12,
                ),
                Text(
                  tradingAccount == null ? "" : tradingAccount.name,
                  style: Styles.blackBoldMedium,
                ),
                tradingAccount == null
                    ? Container()
                    : Form(
                        key: _formKey,
                        autovalidate: true,
                        onChanged: _onFormChanged,
                        child: Padding(
                          padding: const EdgeInsets.only(left: 20.0, right: 48),
                          child: Column(
                            children: <Widget>[
                              SizedBox(
                                height: 20,
                              ),
                              TextFormField(
                                key: _invoiceKey,
                                decoration: InputDecoration(
                                    icon: Icon(Icons.account_balance),
                                    hintText: "Enter Invoice Number",
                                    labelText: "Invoice Number #"),
                                keyboardType: TextInputType.text,
                                validator: (value) {
                                  if (value.isEmpty) {
                                    return 'Please enter invoice number';
                                  }
                                  if (value.length < 3) {
                                    return 'Please enter at least 3 letters or numbers';
                                  }
                                  invoiceNumber = value;
                                  return null;
                                },
                              ),
                              TextFormField(
                                key: _amountKey,
                                decoration: InputDecoration(
                                    icon: Icon(Icons.attach_money),
                                    hintText: "Enter Amount",
                                    labelText: "Amount"),
                                keyboardType: TextInputType.numberWithOptions(
                                    decimal: true),
                                onChanged: _onAmountChanged,
                                validator: (value) {
                                  if (value.isEmpty) {
                                    return 'Please enter amount';
                                  }
                                  amount = value;
                                  return null;
                                },
                              ),
                              SizedBox(
                                height: 20,
                              ),
                              TextFormField(
                                key: _vatKey,
                                onChanged: _onVATChanged,
                                decoration: InputDecoration(
                                    icon: Icon(Icons.note),
                                    hintText: "Enter VAT",
                                    labelText: "Value Added Tax"),
                                keyboardType: TextInputType.numberWithOptions(
                                    decimal: true),
                                validator: (value) {
                                  if (value.isEmpty) {
                                    return 'Please enter VAT %';
                                  }
                                  vat = value;
                                  return null;
                                },
                              ),
                              SizedBox(
                                height: 12,
                              ),
                              TextFormField(
                                key: _descKey,
                                decoration: InputDecoration(
                                    icon: Icon(Icons.event),
                                    hintText: "Enter Description",
                                    labelText: "Description"),
                                keyboardType: TextInputType.text,
                                validator: (value) {
                                  if (value.isEmpty) {
                                    return 'Please enter description';
                                  }
                                  description = value;
                                  return null;
                                },
                              ),
                              SizedBox(
                                height: 12,
                              ),
                              Row(
                                children: <Widget>[
                                  Text(
                                    'Total:',
                                    style: Styles.greyLabelSmall,
                                  ),
                                  SizedBox(
                                    width: 8,
                                  ),
                                  Text(
                                    totalAmount == null ? '0.00' : totalAmount,
                                    style: Styles.blackBoldLarge,
                                  ),
                                ],
                              ),
                              SizedBox(
                                height: 24,
                              ),
                              RaisedButton(
//                                color: Colors.indigo,
                                elevation: 8,
                                onPressed: _onInvoiceSubmitRequested,
                                child: Padding(
                                  padding: const EdgeInsets.all(16.0),
                                  child: Text(
                                    'Submit Invoice',
                                    style: Styles.whiteSmall,
                                  ),
                                ),
                              ),
                              SizedBox(
                                height: 24,
                              ),
                            ],
                          ),
                        ),
                      ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _onFormChanged() {}

  void _onAccountRequested() async {
    print('_onAccountRequested');
    var result = await Navigator.push(
        context,
        SlideRightRoute(
          widget: NetworkAccountsPage(),
        ));
    if (result is AccountInfo) {
      setState(() {
        tradingAccount = result;
      });
    }
  }

  void _onInvoiceSubmitRequested() async {
    print('_onInvoiceSubmitRequested 🍊 🍊 ');
    if (tradingAccount == null) {
      AppSnackbar.showErrorSnackbar(
          scaffoldKey: _key,
          message: 'Please select supplier',
          actionLabel: '');
      return;
    }
    if (_formKey.currentState.validate()) {
      var invoice = Invoice(
        amount: double.parse(amount),
        valueAddedTax: double.parse(vat),
        invoiceNumber: invoiceNumber,
        supplier: tradingAccount,
        customer: account,
        description: description,
      );
      var tot =
          invoice.amount + (invoice.amount * (invoice.valueAddedTax / 100));
      invoice.totalAmount = tot;
      AppSnackbar.showSnackbarWithProgressIndicator(
          scaffoldKey: _key,
          message: 'Submitting invoice ...',
          textColor: Colors.white,
          backgroundColor: Colors.brown);
      try {
        resultInvoice = await Net.startRegisterInvoiceFlow(invoice);
        print(
            ' 🌺  🌺  🌺  INVOICE returned: ${resultInvoice.toJson()}  🌺  🌺  🌺 ');
        AppSnackbar.showSnackbarWithAction(
            scaffoldKey: _key,
            message: 'Invoice submitted OK',
            textColor: Colors.white,
            actionLabel: 'Done',
            listener: this,
            action: 1,
            backgroundColor: Colors.teal[700]);
      } catch (e) {
        AppSnackbar.showErrorSnackbar(
            scaffoldKey: _key, message: e.message, actionLabel: 'Err');
      }
    }
  }

  Invoice resultInvoice;
  void _onAmountChanged(String value) {
    amount = value;
    _setTotalAmount();
  }

  void _onVATChanged(String value) {
    vat = value;
    _setTotalAmount();
  }

  @override
  onActionPressed(int action) {
    switch (action) {
      case 1:
        Navigator.pop(context, resultInvoice);
        break;
    }
  }
}
