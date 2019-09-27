import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnlibrary/util/slide_right.dart';
import 'package:bfnlibrary/util/theme_bloc.dart';
import 'package:bfnmobile/bloc.dart';
import 'package:bfnmobile/ui/buy_offer.dart';
import 'package:bfnmobile/ui/create_invoice.dart';
import 'package:bfnmobile/ui/create_offer.dart';
import 'package:flutter/material.dart';

class InvoicesPage extends StatefulWidget {
  @override
  _InvoicesPageState createState() => _InvoicesPageState();
}

class _InvoicesPageState extends State<InvoicesPage>
    with SingleTickerProviderStateMixin
    implements InvoiceListener {
  List<InvoiceOffer> offers = List();
  List<Invoice> invoices = List();
  AccountInfo account;
  TabController tabController;

  @override
  void initState() {
    super.initState();
    tabController = TabController(length: 3, vsync: this);
    _getInvoicesAndOffers();
  }

  @override
  void dispose() {
    tabController.dispose();
    super.dispose();
  }

  _getInvoicesAndOffers() async {
    account = await bfnBloc.getMyAccount();
    print("My account: 🍊 🍊 🍊 ${account.toJson()} 🍊 🍊 🍊 ");
    offers = await bfnBloc.getInvoiceOffers(
        accountId: bfnBloc.account.identifier, consumed: false);
    invoices = await bfnBloc.getInvoices(accountId: bfnBloc.account.identifier);
    offers.sort((a, b) => b.offerDate.compareTo(a.offerDate));
    invoices.sort((a, b) => b.dateRegistered.compareTo(a.dateRegistered));
    setState(() {});
  }

  String message;

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<int>(
        initialData: themeBloc.themeIndex,
        stream: themeBloc.newThemeStream,
        builder: (context, snapshot) {
          print(
              '👽 👽 👽 👽 main.dart;  snapShot theme index: 👽  ${snapshot.data} 👽 ');
          return MaterialApp(
            debugShowCheckedModeBanner: false,
            theme: snapshot.data == null
                ? ThemeUtil.getTheme(themeIndex: 0)
                : ThemeUtil.getTheme(themeIndex: snapshot.data),
            home: DefaultTabController(
              length: 3,
              child: Scaffold(
                appBar: AppBar(
                  actions: <Widget>[
                    IconButton(
                      icon: Icon(Icons.refresh),
                      onPressed: _getInvoicesAndOffers,
                    ),
                  ],
                  leading: IconButton(
                    icon: Icon(Icons.arrow_back),
                    onPressed: () {
                      Navigator.pop(context);
                    },
                  ),
                  bottom: PreferredSize(
                      child: Column(
                        children: <Widget>[
                          account == null
                              ? Container()
                              : NameBadge(
                                  account: account,
                                  nodeStyle: Styles.whiteSmall,
                                  nameStyle: Styles.whiteBoldMedium,
                                  elevation: 3,
                                ),
                          SizedBox(
                            height: 8,
                          ),
                          StreamBuilder<String>(
                              stream: bfnBloc.fcmStream,
                              initialData: 'No network message yet',
                              builder: (context, snapshot) {
                                if (snapshot.hasData) {
                                  debugPrint(
                                      '😡  😡  😡  😡  FCM message arrived on Stream: ${snapshot.data}  😡  😡  😡  😡 ');
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
                          TabBar(
                            tabs: [
                              Tab(
                                icon: Icon(Icons.list),
                                text: 'Offers',
                              ),
                              Tab(
                                icon: Icon(Icons.apps),
                                text: 'Invoices',
                              ),
                              Tab(
                                icon: Icon(Icons.history),
                                text: 'Journal',
                              ),
                            ],
                          ),
                        ],
                      ),
                      preferredSize: Size.fromHeight(200)),
                  title: Text('Invoices & Offers'),
                ),
                backgroundColor: Colors.brown[100],
                body: TabBarView(
                  children: [
                    OfferList(offers),
                    InvoiceList(
                      account: account,
                      context: context,
                      invoices: invoices,
                      invoiceListener: this,
                    ),
                    Card(
                      child: CreateMenu(),
                    ),
                  ],
                ),
              ),
            ),
          );
        });
  }

  @override
  void onInvoice(Invoice invoice) {
    setState(() {
      invoices.add(invoice);
    });
  }
}

class OfferList extends StatelessWidget {
  final List<InvoiceOffer> offers;

  OfferList(this.offers);

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        ListView.builder(
            itemCount: offers.length,
            itemBuilder: (context, index) {
              var color = Colors.blue[700];
              Color backColor = Colors.white;
              var offer = offers.elementAt(index);
              if (bfnBloc.account.identifier == offer.supplier.identifier) {
                color = Colors.black;
              }
              if (bfnBloc.account.identifier == offer.customer.identifier) {
                color = Colors.grey[400];
              }
              if (bfnBloc.account.identifier == offer.investor.identifier) {
                color = Colors.pink[800];
                backColor = Colors.pink[50];
              }
              return Padding(
                padding: const EdgeInsets.only(top: 8.0, left: 8, right: 8),
                child: GestureDetector(
                  onTap: () {
                    _onOfferTapped(offer, context);
                  },
                  child: Card(
                    elevation: 4,
                    color: backColor,
                    child: ListTile(
                      leading: Icon(
                        Icons.apps,
                        color: color,
                      ),
                      title: Text(
                        getCurrency(offer.offerAmount, context),
                        style: TextStyle(
                            color: color,
                            fontWeight: FontWeight.w900,
                            fontSize: 20),
                      ),
                      subtitle: Column(
                        children: <Widget>[
                          SizedBox(
                            height: 8,
                          ),
                          Row(
                            children: <Widget>[
                              Text(
                                'Customer',
                                style: Styles.greyLabelSmall,
                              ),
                              SizedBox(
                                width: 4,
                              ),
                              Text(offer.customer.name),
                            ],
                          ),
                          Row(
                            children: <Widget>[
                              Text(
                                'Supplier',
                                style: Styles.greyLabelSmall,
                              ),
                              SizedBox(
                                width: 4,
                              ),
                              Text(offer.supplier.name),
                            ],
                          ),
                          Row(
                            children: <Widget>[
                              Text(
                                'Buyer',
                                style: Styles.greyLabelSmall,
                              ),
                              SizedBox(
                                width: 4,
                              ),
                              Text(
                                offer.investor.name,
                                style: Styles.blackBoldSmall,
                              ),
                            ],
                          ),
                          Row(
                            children: <Widget>[
                              Text(
                                'Invoice Amount',
                                style: Styles.greyLabelSmall,
                              ),
                              SizedBox(
                                width: 4,
                              ),
                              Text(getCurrency(offer.originalAmount, context)),
                            ],
                          ),
                          Row(
                            children: <Widget>[
                              Text(
                                'Discount',
                                style: Styles.greyLabelSmall,
                              ),
                              SizedBox(
                                width: 4,
                              ),
                              Text(
                                '${getCurrency(offer.discount, context)} %',
                                style: Styles.tealBoldSmall,
                              ),
                            ],
                          ),
                          SizedBox(
                            height: 12,
                          ),
                          Row(
                            children: <Widget>[
                              Text(
                                'Offered',
                                style: Styles.greyLabelSmall,
                              ),
                              SizedBox(
                                width: 4,
                              ),
                              Text(
                                getFormattedDateShortWithTime(
                                    offer.offerDate, context),
                                style: Styles.blackBoldSmall,
                              ),
                            ],
                          ),
                          SizedBox(
                            height: 20,
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              );
            }),
        Positioned(
          top: 20,
          right: 0,
          child: Container(
            height: 60,
            width: 80,
            child: Card(
              elevation: 20,
              color: Colors.yellow,
              child: Center(
                child: Text(
                  '${offers.length}',
                  style: Styles.pinkBoldMedium,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }

  _onOfferTapped(InvoiceOffer offer, BuildContext context) async {
    //check what action to take ...
    if (bfnBloc.account.identifier == offer.supplier.identifier) {
      //just observing ...
    }
    if (bfnBloc.account.identifier == offer.customer.identifier) {
      //just observing ...
    }
    if (bfnBloc.account.identifier == offer.investor.identifier) {
      //a buyInvoice is possible ...
      Navigator.push(
          context,
          SlideRightRoute(
            widget: BuyOffer(offer),
          ));
    }
  }

  String getCurrency(double amt, BuildContext context) {
    return getFormattedAmount(amt.toString(), context);
  }
}

class CreateMenu extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Center(
        child: Column(
          children: <Widget>[
            SizedBox(
              height: 40,
            ),
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Text(
                  'Every invoice recorded on the Network should be from a customer who is already part of the Network. To create an invoice you must select your customer from the Accounts list'),
            ),
            SizedBox(
              height: 20,
            ),
            RaisedButton(
              color: Colors.pink,
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text(
                  'Create Invoice',
                  style: Styles.whiteSmall,
                ),
              ),
              onPressed: _createInvoicePressed,
            ),
            SizedBox(
              height: 20,
            ),
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Text(
                  'Every invoice recorded on the Network should be from a customer who is already part of the Network. To create an invoice you must select your customer from the Accounts list'),
            ),
            SizedBox(
              height: 20,
            ),
            RaisedButton(
              color: Colors.indigo,
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text(
                  'Create Offer',
                  style: Styles.whiteSmall,
                ),
              ),
              onPressed: _createInvoiceOfferPressed,
            ),
          ],
        ),
      ),
    );
  }

  void _createInvoicePressed() {}

  void _createInvoiceOfferPressed() {}
}

abstract class InvoiceListener {
  void onInvoice(Invoice invoice);
}

class InvoiceList extends StatelessWidget {
  final List<Invoice> invoices;
  final BuildContext context;
  final AccountInfo account;
  final InvoiceListener invoiceListener;

  InvoiceList(
      {this.invoices, this.context, this.account, this.invoiceListener});

  _checkOffers(Invoice invoice) {
    print('checkOffers  😎 😎  😎 😎  😎 😎 ${invoice.invoiceNumber}');
    Navigator.pop(context);
  }

  void _displayDialog(Invoice invoice) async {
    print(
        '👽👽👽 Invoice Selected: 👽👽👽 ${invoice.toJson()} 👽👽👽 check me:  ${account.identifier} vs customer: ${invoice.customer.identifier}');
    if (invoice.customer.identifier == account.identifier) {
      showDialog(
          context: context,
          builder: (context) {
            return AlertDialog(
              title: Text('My Invoice Detail'),
              content: new InvoiceDetail(invoice),
              actions: <Widget>[
                FlatButton(
                  onPressed: () {
                    _checkOffers(invoice);
                  },
                  child: Text(
                    'Check Offers',
                    style: Styles.blueBoldSmall,
                  ),
                )
              ],
            );
          });
    } else {
      showDialog(
          context: context,
          builder: (context) {
            return AlertDialog(
              title: Text('Invoice Actions'),
              content: new InvoiceDetail(invoice),
              actions: <Widget>[
                FlatButton(
                    onPressed: () {
                      _sendMessage(invoice);
                    },
                    child: Text('Send Message')),
                FlatButton(
                  onPressed: () {
                    _createOffer(invoice);
                  },
                  child: Text(
                    'Create Offer',
                    style: Styles.blueBoldSmall,
                  ),
                )
              ],
            );
          });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        ListView.builder(
            itemCount: invoices.length,
            itemBuilder: (context, index) {
              var color = Colors.pink[700];
              var invoice = invoices.elementAt(index);
              if (bfnBloc.account.identifier == invoice.customer.identifier) {
                color = Colors.blue[700];
              }
              return Padding(
                padding: const EdgeInsets.only(top: 8.0, left: 20, right: 20),
                child: GestureDetector(
                  onTap: () {
                    _displayDialog(invoices.elementAt(index));
                  },
                  child: Card(
                    elevation: 4,
                    child: ListTile(
                      leading: Icon(
                        Icons.account_balance,
                        color: Colors.black,
                      ),
                      title: Text(
                        getCurrency(invoice.totalAmount, context),
                        style: TextStyle(
                            color: color,
                            fontWeight: FontWeight.w900,
                            fontSize: 20),
                      ),
                      subtitle: Column(
                        children: <Widget>[
                          SizedBox(
                            height: 8,
                          ),
                          Row(
                            children: <Widget>[
                              Text(
                                'From:',
                                style: Styles.greyLabelSmall,
                              ),
                              SizedBox(
                                width: 8,
                              ),
                              Text(
                                invoice.customer.name,
                                style: Styles.greyLabelSmall,
                              ),
                            ],
                          ),
                          SizedBox(
                            height: 2,
                          ),
                          Row(
                            children: <Widget>[
                              Text(
                                'Issued To:',
                                style: Styles.greyLabelSmall,
                              ),
                              SizedBox(
                                width: 8,
                              ),
                              Text(
                                invoice.supplier.name,
                                style: Styles.blackBoldSmall,
                              ),
                            ],
                          ),
                          SizedBox(
                            height: 8,
                          ),
                          Row(
                            children: <Widget>[
                              Text(getFormattedDateLongWithTime(
                                  invoice.dateRegistered, context)),
                            ],
                          ),
                          SizedBox(
                            height: 20,
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              );
            }),
        Positioned(
          top: 20,
          right: 0,
          child: Container(
            height: 60,
            width: 80,
            child: GestureDetector(
              onTap: _onTotalRequested,
              child: Card(
                elevation: 20,
                color: Colors.teal[200],
                child: Center(
                  child: Text(
                    '${invoices.length}',
                    style: Styles.whiteBoldMedium,
                  ),
                ),
              ),
            ),
          ),
        ),
        Positioned(
          bottom: 12,
          right: 12,
          child: FloatingActionButton(
            backgroundColor: Colors.pink[700],
            elevation: 16,
            child: Icon(Icons.account_balance),
            onPressed: _onAddInvoice,
          ),
        ),
      ],
    );
  }

  String getCurrency(double amt, BuildContext context) {
    return getFormattedAmount(amt.toString(), context);
  }

  void _onAddInvoice() async {
    print('onAddInvoice 🍊 🍊 🍊 🍊 🍊 🍊 🍊 ');
    var res = await Navigator.push(
        context,
        SlideRightRoute(
          widget: CreateInvoice(),
        ));
    if (res is Invoice) {
      print(
          '🧩 🧩 🧩 🧩 🧩 Yebo!! - invoice created and returned: 🧩 🧩 🧩 🧩 🧩 ${res.toJson()} 🧩 🧩 🧩 🧩 🧩 ');
      invoiceListener.onInvoice(res);
    }
  }

  void _onTotalRequested() {
    print('_onTotalRequested  😎 😎');
  }

  void _sendMessage(Invoice invoice) {
    print(' 🌺  🌺  🌺 _sendMessage ...............');
    Navigator.pop(context);
  }

  void _createOffer(Invoice invoice) {
    print(' 🌺  🌺  🌺 _createOffer ...............');
    Navigator.pop(context);
    Navigator.push(
        context,
        SlideRightRoute(
          widget: CreateOffer(invoice),
        ));
  }
}

class InvoiceDetail extends StatelessWidget {
  final Invoice invoice;

  InvoiceDetail(this.invoice);

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 200,
      child: Column(
        children: <Widget>[
          Column(
            children: <Widget>[
              Text(
                'Customer:',
                style: Styles.greyLabelSmall,
              ),
              SizedBox(
                width: 8,
              ),
              Text(invoice.customer.name),
            ],
          ),
          SizedBox(
            height: 20,
          ),
          Column(
            children: <Widget>[
              Text('Supplier:', style: Styles.greyLabelSmall),
              SizedBox(
                width: 8,
              ),
              Text(invoice.supplier.name, style: Styles.blackBoldMedium),
            ],
          ),
          SizedBox(
            height: 20,
          ),
          Column(
            children: <Widget>[
              Text('Total Amount:', style: Styles.greyLabelSmall),
              SizedBox(
                width: 8,
              ),
              Text(
                getFormattedAmount('${invoice.totalAmount}', context),
                style: Styles.blackBoldLarge,
              ),
            ],
          ),
          SizedBox(
            height: 8,
          ),
          Row(
            children: <Widget>[
              SizedBox(
                width: 8,
              ),
              Text(getFormattedDateLongWithTime(
                  invoice.dateRegistered, context)),
            ],
          ),
        ],
      ),
    );
  }
}
