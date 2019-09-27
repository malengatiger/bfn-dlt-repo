import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:bfnlibrary/util/prefs.dart';
import 'package:bfnlibrary/util/slide_right.dart';
import 'package:bfnlibrary/util/snack.dart';
import 'package:bfnmobile/ui/list_tabs.dart';
import 'package:bfnmobile/ui/network_accounts.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import 'buy_offer.dart';

class CreateOffer extends StatefulWidget {
  final Invoice invoice;

  CreateOffer(this.invoice);

  @override
  _CreateOfferState createState() => _CreateOfferState();
}

TextEditingController _controller = TextEditingController();

class _CreateOfferState extends State<CreateOffer> implements SnackBarListener {
  var _key = GlobalKey<ScaffoldState>();
  final _formKey = GlobalKey<FormState>();
  AccountInfo account;
  @override
  initState() {
    super.initState();
    _getAccount();
  }

  _getAccount() async {
    account = await Prefs.getAccount();
    setState(() {});
  }

  String discount;
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        title: Text('Create Offer'),
        bottom: PreferredSize(
          preferredSize: Size.fromHeight(80),
          child: Column(
            children: <Widget>[
              NameBadge(
                account: account,
                nodeStyle: Styles.whiteSmall,
                nameStyle: Styles.blackBoldMedium,
                elevation: 2,
              ),
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
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Container(child: InvoiceDetail(widget.invoice)),
          ),
          SizedBox(
            height: 40,
          ),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Card(
              elevation: 4,
              child: Column(
                children: <Widget>[
                  SizedBox(
                    height: 20,
                  ),
                  Text(
                    'Make Offer',
                    style: Styles.blackBoldLarge,
                  ),
                  SizedBox(
                    height: 8,
                  ),
                  RaisedButton(
                    onPressed: _getTradingAccount,
                    elevation: 2,
                    child: Text(
                      'Select Investor',
                      style: Styles.whiteSmall,
                    ),
                  ),
                  tradingAccount == null
                      ? Container()
                      : Text(
                          tradingAccount.name,
                          style: Styles.blueBoldMedium,
                        ),
                  SizedBox(
                    height: 8,
                  ),
                  tradingAccount == null
                      ? Container()
                      : Padding(
                          padding: const EdgeInsets.only(
                              left: 48, bottom: 20, right: 48),
                          child: TextField(
                            controller: _controller,
                            style: Styles.pinkBoldLarge,
                            keyboardType:
                                TextInputType.numberWithOptions(decimal: true),
                            decoration: InputDecoration(
                                labelText: 'Discount %',
                                hintText: 'Enter Discount',
                                hintStyle: Styles.pinkBoldLarge),
                            onChanged: _onDiscountChanged,
                          ),
                        ),
                  tradingAccount == null
                      ? Container()
                      : RaisedButton(
                          color: Colors.indigo,
                          elevation: 8,
                          onPressed: _submitOffer,
                          child: Padding(
                            padding: const EdgeInsets.all(20.0),
                            child: Text(
                              'Submit Offer',
                              style: Styles.whiteSmall,
                            ),
                          ),
                        ),
                  SizedBox(
                    height: 20,
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _submitOffer() async {
    print('_submitOffer  🌺  😎 😎 discount is : $discount');
    if (discount == null || discount.isEmpty) {
      AppSnackbar.showErrorSnackbar(
          scaffoldKey: _key,
          message: "Please enter Discount",
          actionLabel: 'Error');
      return;
    }
    double myDisc = double.parse(discount);
    if (myDisc == 0.0) {
      AppSnackbar.showErrorSnackbar(
          scaffoldKey: _key,
          message: "Please enter valid Discount > 0",
          actionLabel: 'Error');
      return;
    }
    var acct = await Prefs.getAccount();
    var invoiceOffer = InvoiceOffer(
      discount: myDisc,
      investor: tradingAccount,
      supplier: widget.invoice.supplier,
      customer: widget.invoice.customer,
      owner: widget.invoice.supplier,
      originalAmount: widget.invoice.totalAmount,
      invoiceId: widget.invoice.invoiceId,
    );
    AppSnackbar.showSnackbarWithProgressIndicator(
        scaffoldKey: _key,
        message: 'Submitting Offer ...',
        textColor: Colors.lightGreen,
        backgroundColor: Colors.black);
    try {
      invoiceOffer = await Net.startInvoiceOfferFlow(invoiceOffer);
      invoiceOfferResult = invoiceOffer;
      AppSnackbar.showSnackbarWithAction(
          scaffoldKey: _key,
          message: "Offer submitted OK",
          textColor: Colors.white,
          backgroundColor: Colors.teal,
          actionLabel: 'OK',
          listener: this);
    } catch (e) {
      AppSnackbar.showErrorSnackbar(
          scaffoldKey: _key, message: 'Submission failed', actionLabel: '');
    }
  }

  AccountInfo tradingAccount;
  InvoiceOffer invoiceOfferResult;

  void _getTradingAccount() async {
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

  void _onDiscountChanged(String value) {
    discount = value;
    print('discount: $discount');
  }

  @override
  onActionPressed(int action) {
    Navigator.pop(context, invoiceOfferResult);
  }
}
