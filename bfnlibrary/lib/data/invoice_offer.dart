import 'package:bfnlibrary/data/account.dart';

class InvoiceOffer {
  String invoiceId;
  AccountInfo owner;
  AccountInfo supplier, investor;
  String offerDate;
  double offerAmount, discount;
  String investorDate;

  InvoiceOffer(this.invoiceId, this.owner, this.supplier, this.investor,
      this.offerDate, this.offerAmount, this.discount, this.investorDate);

  InvoiceOffer.fromJson(Map data) {
    this.invoiceId = data['invoiceId'];
    if (data['owner'] != null) {
      this.owner = AccountInfo.fromJson(data['owner']);
    }
    if (data['supplier'] != null) {
      this.supplier = AccountInfo.fromJson(data['supplier']);
    }
    if (data['investor'] != null) {
      this.investor = AccountInfo.fromJson(data['investor']);
    }
    this.offerDate = data['offerDate'];
    if (data['offerAmount'] is int) {
      this.offerAmount = data['offerAmount'] * 1.00;
    }
    if (data['offerAmount'] is double) {
      this.offerAmount = data['offerAmount'];
    }
    if (data['discount'] is int) {
      this.discount = data['discount'] * 1.00;
    }
    if (data['discount'] is double) {
      this.discount = data['discount'];
    }
    this.investorDate = data['investorDate'];
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'invoiceId': invoiceId,
        'owner': owner,
        'supplier': supplier,
        'offerAmount': offerAmount,
        'discount': discount,
        'investorDate': investorDate,
        'offerDate': offerDate,
        'investor': investor,
      };
}
