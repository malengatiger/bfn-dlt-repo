class InvoiceOffer {
  String invoiceId;
  String ownerId;
  String supplierId, investorId, offerDate;
  double offerAmount, discount;
  String investorDate;

  InvoiceOffer(
      this.invoiceId,
      this.ownerId,
      this.supplierId,
      this.investorId,
      this.offerDate,
      this.offerAmount,
      this.discount,
      this.investorDate);

  InvoiceOffer.fromJson(Map data) {
    this.invoiceId = data['invoiceId'];
    this.ownerId = data['ownerId'];
    this.supplierId = data['supplierId'];
    this.investorId = data['investorId'];
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
        'ownerId': ownerId,
        'supplierId': supplierId,
        'offerAmount': offerAmount,
        'discount': discount,
        'investorDate': investorDate,
        'offerDate': offerDate,
        'investorId': investorId,
      };
}
