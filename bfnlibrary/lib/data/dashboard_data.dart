class DashboardData {
  int invoices;
  int accounts, offers;
  String node;

  DashboardData(this.invoices, this.accounts, this.offers, this.node);

  DashboardData.fromJson(Map data) {
    this.invoices = data['invoices'];
    this.accounts = data['accounts'];
    this.offers = data['offers'];
    this.node = data['webAPIUrl'];
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'offers': offers,
        'invoices': invoices,
        'accounts': accounts,
        'node': node,
      };
}
