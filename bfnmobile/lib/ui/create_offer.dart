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

import '../bloc.dart';
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

  String discount, message;
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        title: Text('Create Offer'),
        bottom: PreferredSize(
          preferredSize: Size.fromHeight(100),
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
              StreamBuilder<String>(
                  stream: bfnBloc.fcmStream,
                  initialData: 'No network message yet',
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      debugPrint(
                          '😡  😡  😡  😡  CreateOffer: FCM message arrived on Stream: ${snapshot.data}  😡  😡  😡  😡 ');
                      message = snapshot.data;
                    }
                    return Text(
                      '$message',
                      style: Styles.whiteSmall,
                    );
                  }),
              SizedBox(
                height: 8,
              ),
            ],
          ),
        ),
      ),
      backgroundColor: Colors.brown[50],
      body: _getBody(),
    );
  }

  Widget _getBody() {
    return ListView(
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Card(
              color: Colors.grey[200], child: InvoiceDetail(widget.invoice)),
        ),
        Padding(
          padding: const EdgeInsets.only(left: 8, right: 8),
          child: Card(
            elevation: 2,
            child: Column(
              children: <Widget>[
                SizedBox(
                  height: 12,
                ),
                FlatButton(
                  onPressed: _getTradingAccount,
                  child: Text(
                    'Select Investor',
                    style: Styles.blueMedium,
                  ),
                ),
                tradingAccount == null
                    ? Container()
                    : Text(
                        tradingAccount.name,
                        style: Styles.blackBoldMedium,
                      ),
                SizedBox(
                  height: 8,
                ),
                discount == null
                    ? Container()
                    : Text(
                        getFormattedAmount(discountAmount, context),
                        style: Styles.tealBoldMedium,
                      ),
                SizedBox(
                  height: 8,
                ),
                tradingAccount == null
                    ? Container()
                    : Padding(
                        padding: const EdgeInsets.only(
                            left: 96, bottom: 20, right: 96),
                        child: TextField(
                          controller: _controller,
                          style: Styles.blackBoldMedium,
                          keyboardType:
                              TextInputType.numberWithOptions(decimal: true),
                          decoration: InputDecoration(
                              labelText: 'Discount %',
                              hintText: '0.0',
                              hintStyle: Styles.blackBoldMedium),
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
                          padding: const EdgeInsets.all(8.0),
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
  String discountAmount;

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

    var num = double.parse(discount);
    var m = widget.invoice.totalAmount * (num / 100);
    print(' 😡  😡 discount: $discount 👽 👽 discount amount: $m');
    setState(() {
      discountAmount = m.toString();
    });
  }

  @override
  onActionPressed(int action) {
    Navigator.pop(context, invoiceOfferResult);
  }
}
